package com.finance.dart.common.interceptor;

import com.finance.dart.common.config.EndPointConfig;
import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.exception.BizException;
import com.finance.dart.member.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

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

        //@2. 로그인 세션 확인
        sessionService.sessionCheckErrResponse(request, response);

        //@3. 권한 체크 (어노테이션)
        EndPointConfig.RequireRole requireRole =
                handlerMethod.getMethodAnnotation(EndPointConfig.RequireRole.class);

        // 사용자의 현재 권한 목록
        List<String> userRoles = sessionService.getRolesFromSession(request);

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

        // 사용자 권한세션 갱신(TTL 갱신)
        sessionService.updateLoginRolesTTL(request, userRoles);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);

        if(log.isDebugEnabled()) log.debug("후처리");

    }
}