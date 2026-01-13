package com.finance.dart.common.advice;

import com.finance.dart.member.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * ResponseBodyAdvice를 사용한 세션 쿠키 TTL 갱신
 *
 * HttpMessageConverter가 응답을 커밋하기 전에 실행되므로
 * @RestController 환경에서도 쿠키 헤더를 정상적으로 추가할 수 있습니다.
 */
@Slf4j
@AllArgsConstructor
@ControllerAdvice
public class SessionCookieRefreshAdvice implements ResponseBodyAdvice<Object> {

    private final SessionService sessionService;

    /**
     * 이 Advice를 적용할지 결정
     * @return true면 beforeBodyWrite() 실행
     */
    @Override
    public boolean supports(MethodParameter returnType,
                           Class<? extends HttpMessageConverter<?>> converterType) {
        // 모든 @RestController 응답에 적용
        return true;
    }

    /**
     * HttpMessageConverter가 응답을 쓰기 직전에 호출됨
     * 이 시점에서 쿠키 헤더를 추가하면 정상적으로 브라우저에 전달됨
     */
    @Override
    public Object beforeBodyWrite(Object body,
                                 MethodParameter returnType,
                                 MediaType selectedContentType,
                                 Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                 ServerHttpRequest serverRequest,
                                 ServerHttpResponse serverResponse) {

        // ServerHttpRequest를 HttpServletRequest로 변환
        if (!(serverRequest instanceof ServletServerHttpRequest)) {
            return body;
        }

        HttpServletRequest request = ((ServletServerHttpRequest) serverRequest).getServletRequest();

        // 세션 TTL 갱신 (Redis + 쿠키 동시 갱신)
        String sessionId = sessionService.getSessionId(request);

        if (sessionId != null && sessionService.sessionCheck(request)) {
            String requestUri = request.getRequestURI();

            // TTL 갱신 제외 URL 체크
            if (!sessionService.isExcludedFromTtlRefresh(requestUri)) {
                // Redis TTL 갱신
                sessionService.refreshSessionTtl(sessionId);

                // 쿠키 TTL 갱신
                ResponseCookie cookie = sessionService.createSessionCookie(sessionId);
                serverResponse.getHeaders().add(HttpHeaders.SET_COOKIE, cookie.toString());

                if(log.isDebugEnabled()) {
                    log.debug("Session TTL refreshed (ResponseBodyAdvice) - SessionId: {}, URI: {}, Method: {}, Set-Cookie: {}",
                            sessionId, requestUri, request.getMethod(), cookie.toString());
                }
            } else {
                if(log.isDebugEnabled()) {
                    log.debug("TTL refresh excluded (ResponseBodyAdvice) - URI: {}", requestUri);
                }
            }
        } else {
            if(log.isDebugEnabled()) {
                log.debug("No session or invalid session (ResponseBodyAdvice) - SessionId: {}", sessionId);
            }
        }

        // 응답 바디는 그대로 반환
        return body;
    }
}
