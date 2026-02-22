package com.finance.dart.monitoring.tracker;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HTTP 요청 트래픽 카운터 + ThreadLocal 기반 사용자 요청 컨텍스트
 *
 * Servlet Filter로 등록되어:
 * 1. 모든 사용자 HTTP 요청을 카운트 (Web → Backend)
 * 2. ThreadLocal로 "사용자 요청 처리 중" 표시 → AOP에서 다운스트림 호출 추적에 활용
 *    (모니터링 @Scheduled 헬스체크는 ThreadLocal이 없으므로 자동 제외)
 */
@Component
public class RequestTrafficTracker implements Filter {

    private static final ThreadLocal<Boolean> IN_USER_REQUEST = new ThreadLocal<>();

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpReq) {
            String uri = httpReq.getRequestURI();
            if (!uri.contains("/monitoring/") && !uri.contains("/actuator/")) {
                counter.incrementAndGet();
                IN_USER_REQUEST.set(true);
                try {
                    chain.doFilter(request, response);
                } finally {
                    IN_USER_REQUEST.remove();
                }
                return;
            }
        }
        chain.doFilter(request, response);
    }

    /** 현재 스레드가 사용자 HTTP 요청 처리 중인지 (Scheduled 등은 false) */
    public static boolean isInUserRequest() {
        return Boolean.TRUE.equals(IN_USER_REQUEST.get());
    }

    public int getAndReset() {
        return counter.getAndSet(0);
    }

    public int get() {
        return counter.get();
    }
}
