package com.finance.dart.common.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import org.slf4j.Marker;

/**
 * 스케줄러 스레드(@Scheduled)에서 발생하는 DEBUG/TRACE 로그를 차단하는 필터.
 *
 * 모니터링 스케줄러가 1~5초 주기로 DB를 조회하면서 Hibernate가 찍는 DEBUG 스팸이
 * 콘솔을 가득 채우는 문제를 막는다. 스케줄러 스레드 이름(scheduling-*)으로 식별하므로
 * 실제 HTTP 요청(http-nio-*)에서 나오는 SQL/DEBUG 로그는 영향받지 않는다.
 *
 * 진짜 장애(INFO/WARN/ERROR)는 스케줄러에서도 그대로 출력된다.
 */
public class SchedulerNoiseFilter extends TurboFilter {

    /** Spring 기본 TaskScheduler 가 생성하는 스레드 이름 접두사 */
    private static final String SCHEDULER_THREAD_PREFIX = "scheduling-";

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level,
                              String format, Object[] params, Throwable t) {
        // DEBUG 이하(DEBUG, TRACE)만 차단 대상
        if (level.toInt() > Level.DEBUG.toInt()) {
            return FilterReply.NEUTRAL;
        }

        String threadName = Thread.currentThread().getName();
        if (threadName != null && threadName.startsWith(SCHEDULER_THREAD_PREFIX)) {
            return FilterReply.DENY;
        }

        return FilterReply.NEUTRAL;
    }
}
