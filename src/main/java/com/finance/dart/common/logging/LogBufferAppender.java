package com.finance.dart.common.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Logback 커스텀 Appender - 로그를 메모리 버퍼에 저장
 */
public class LogBufferAppender extends AppenderBase<ILoggingEvent> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static CircularLogBuffer logBuffer;

    /**
     * Spring ApplicationContext에서 CircularLogBuffer Bean 주입
     */
    public static void setLogBuffer(CircularLogBuffer buffer) {
        logBuffer = buffer;
    }

    @Override
    protected void append(ILoggingEvent event) {
        if (logBuffer == null) {
            return;
        }

        // 로그 메시지 포맷팅
        String formattedLog = formatLog(event);
        logBuffer.append(formattedLog);
    }

    /**
     * 로그 포맷팅
     */
    private String formatLog(ILoggingEvent event) {
        StringBuilder sb = new StringBuilder();

        // 시간
        sb.append(LocalDateTime.now().format(FORMATTER));
        sb.append(" ");

        // 로그 레벨
        sb.append(String.format("%-5s", event.getLevel()));
        sb.append(" ");

        // 스레드명
        sb.append("[").append(event.getThreadName()).append("]");
        sb.append(" ");

        // 로거명 (패키지명 축약)
        String loggerName = event.getLoggerName();
        if (loggerName.length() > 40) {
            loggerName = "..." + loggerName.substring(loggerName.length() - 37);
        }
        sb.append(String.format("%-40s", loggerName));
        sb.append(" : ");

        // 메시지
        sb.append(event.getFormattedMessage());

        // 예외 스택트레이스
        if (event.getThrowableProxy() != null) {
            sb.append("\n").append(event.getThrowableProxy().toString());
        }

        return sb.toString();
    }
}
