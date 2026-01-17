package com.finance.dart.common.logging;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

/**
 * LogBuffer 초기화 설정
 */
@Configuration
@RequiredArgsConstructor
public class LogBufferConfiguration {

    private final CircularLogBuffer logBuffer;

    @PostConstruct
    public void init() {
        // Logback Appender에 Spring Bean 주입
        LogBufferAppender.setLogBuffer(logBuffer);
    }
}
