package com.finance.dart.api.abroad.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * FMP API 슬라이딩 윈도우 Rate Limiter
 * - 분당 300건 제한 대응
 * - SOFT_THRESHOLD(250건): 종목 간 체크 포인트에서 대기
 * - HARD_THRESHOLD(290건): 개별 호출 전 안전망
 */
@Slf4j
@Component
public class FmpRateLimiter {

    private static final int WINDOW_MS = 60_000;        // 1분 윈도우
    private static final int SOFT_THRESHOLD = 250;       // 종목 간 체크 임계값
    private static final int HARD_THRESHOLD = 290;       // 개별 호출 안전망
    private static final int MAX_WAIT_MS = 65_000;       // 최대 대기 시간

    private final Deque<Long> callTimestamps = new ConcurrentLinkedDeque<>();

    /** 만료된 타임스탬프 제거 */
    private void purgeExpired() {
        long cutoff = System.currentTimeMillis() - WINDOW_MS;
        while (!callTimestamps.isEmpty() && callTimestamps.peekFirst() < cutoff) {
            callTimestamps.pollFirst();
        }
    }

    /** FMP 호출 1건 기록 */
    public synchronized void recordCall() {
        callTimestamps.addLast(System.currentTimeMillis());
    }

    /** N건 동시 기록 (parallel 호출용) */
    public synchronized void recordCalls(int count) {
        long now = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            callTimestamps.addLast(now);
        }
    }

    /** 현재 윈도우 내 호출 수 */
    public synchronized int getCallCountInWindow() {
        purgeExpired();
        return callTimestamps.size();
    }

    /** 종목 간 호출 — SOFT_THRESHOLD 초과 시 대기 */
    public void waitIfNeeded() {
        waitUntilBelow(SOFT_THRESHOLD);
    }

    /** 개별 호출 전 — HARD_THRESHOLD 안전망 */
    public void waitIfHardLimit() {
        waitUntilBelow(HARD_THRESHOLD);
    }

    private void waitUntilBelow(int threshold) {
        long waitStart = System.currentTimeMillis();

        while (true) {
            int count;
            long oldestTs;

            synchronized (this) {
                purgeExpired();
                count = callTimestamps.size();
                if (count < threshold) return;
                oldestTs = callTimestamps.peekFirst() != null ? callTimestamps.peekFirst() : System.currentTimeMillis();
            }

            // 최대 대기 시간 초과
            if (System.currentTimeMillis() - waitStart > MAX_WAIT_MS) {
                log.warn("[FmpRateLimiter] 최대 대기시간({}ms) 초과. 현재 {}건, 임계값 {}", MAX_WAIT_MS, count, threshold);
                return;
            }

            // 가장 오래된 호출이 윈도우 밖으로 나갈 때까지 대기
            long sleepMs = oldestTs + WINDOW_MS - System.currentTimeMillis() + 100; // 100ms 여유
            if (sleepMs > 0) {
                log.info("[FmpRateLimiter] Rate limit 대기: 현재 {}건/{} 임계값, {}ms 대기", count, threshold, sleepMs);
                try {
                    Thread.sleep(Math.min(sleepMs, MAX_WAIT_MS));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
