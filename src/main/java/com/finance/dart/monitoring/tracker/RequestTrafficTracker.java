package com.finance.dart.monitoring.tracker;

import com.finance.dart.monitoring.dto.ApiRequestLogDto;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * HTTP 요청 트래픽 카운터 + ThreadLocal 기반 사용자 요청 컨텍스트
 *
 * Servlet Filter로 등록되어:
 * 1. 모든 사용자 HTTP 요청을 카운트 (Web → Backend)
 * 2. ThreadLocal로 "사용자 요청 처리 중" 표시 → AOP에서 다운스트림 호출 추적에 활용
 *    (모니터링 @Scheduled 헬스체크는 ThreadLocal이 없으므로 자동 제외)
 * 3. 개별 요청 로그 캡처 → Service Activity 카드 표시용
 */
@Component
public class RequestTrafficTracker implements Filter {

    private static final ThreadLocal<Boolean> IN_USER_REQUEST = new ThreadLocal<>();
    private static final int MAX_LOG_BUFFER = 200;

    private final AtomicInteger counter = new AtomicInteger(0);
    private final ConcurrentLinkedDeque<ApiRequestLogDto> requestLogBuffer = new ConcurrentLinkedDeque<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest httpReq) {
            String uri = httpReq.getRequestURI();
            if (!uri.contains("/monitoring/") && !uri.contains("/actuator/")) {
                counter.incrementAndGet();
                IN_USER_REQUEST.set(true);
                long startNano = System.nanoTime();
                try {
                    chain.doFilter(request, response);
                } finally {
                    IN_USER_REQUEST.remove();
                    long durationMs = (System.nanoTime() - startNano) / 1_000_000;
                    int status = (response instanceof HttpServletResponse httpResp)
                            ? httpResp.getStatus() : 200;
                    ApiRequestLogDto logEntry = ApiRequestLogDto.builder()
                            .method(httpReq.getMethod())
                            .uri(uri)
                            .status(status)
                            .durationMs(durationMs)
                            .timestamp(System.currentTimeMillis())
                            .build();
                    requestLogBuffer.addLast(logEntry);
                    // 안전 캡: 오래된 항목 제거
                    while (requestLogBuffer.size() > MAX_LOG_BUFFER) {
                        requestLogBuffer.pollFirst();
                    }
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

    /** 버퍼에 쌓인 요청 로그를 drain하여 반환 */
    public List<ApiRequestLogDto> drainRequestLogs() {
        List<ApiRequestLogDto> drained = new ArrayList<>();
        ApiRequestLogDto entry;
        while ((entry = requestLogBuffer.pollFirst()) != null) {
            drained.add(entry);
        }
        return drained;
    }

    public int getAndReset() {
        return counter.getAndSet(0);
    }

    public int get() {
        return counter.get();
    }
}
