package com.finance.dart.common.interceptor;

import com.finance.dart.common.config.EndPointConfig;
import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.exception.BizException;
import com.finance.dart.member.dto.LoginDTO;
import com.finance.dart.member.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

@Slf4j
@AllArgsConstructor
@Component
public class CommonInterceptor implements HandlerInterceptor {

    private final SessionService sessionService;


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        if(log.isDebugEnabled()) log.debug("선처리");

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        //@1. Public 엔드포인트 체크 (어노테이션)
        if (handlerMethod.hasMethodAnnotation(EndPointConfig.PublicEndpoint.class)) {
            return true;
        }

        //@2. 로그인 세션 확인 (TTL 갱신은 ResponseBodyAdvice에서 수행)
        sessionService.sessionCheckOnly(request, response);

        //@3. 로그인 회원정보
        LoginDTO loginDTO = sessionService.getLoginInfo(request);

        if (loginDTO == null || loginDTO.getRoles() == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            throw new BizException(ResponseEnum.LOGIN_SESSION_EXPIRED);
        }

        //@4. 권한 체크 (어노테이션)
        EndPointConfig.RequireRole requireRole =
                handlerMethod.getMethodAnnotation(EndPointConfig.RequireRole.class);

        // 사용자의 현재 권한 목록
        List<String> userRoles = loginDTO.getRoles();

        if (requireRole != null) {
            // 필요한 권한 목록
            String[] requiredRoles = requireRole.value();

            // 필요한 권한 중 하나라도 가지고 있는지 확인
            boolean hasRequiredRole = Arrays.stream(requiredRoles)
                    .anyMatch(userRoles::contains);

            if (!hasRequiredRole) {
                if(log.isDebugEnabled()) {
                    log.debug("권한 부족 - 필요 권한: {}, 현재 권한: {}",
                            Arrays.toString(requiredRoles), userRoles);
                }
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
                throw new BizException(ResponseEnum.FORBIDDEN);
            }
        }

        return true;
    }

    // postHandle()에서 쿠키 TTL 갱신 로직 제거
    // @RestController 환경에서는 postHandle() 시점에 이미 response가 커밋되어 쿠키 헤더 추가가 불가능
    // 대신 SessionCookieRefreshAdvice (ResponseBodyAdvice)에서 처리
}