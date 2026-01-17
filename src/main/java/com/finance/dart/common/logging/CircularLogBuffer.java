package com.finance.dart.common.logging;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * 최근 N개의 로그를 메모리에 보관하는 순환 버퍼
 */
@Component
public class CircularLogBuffer {

    private static final int MAX_SIZE = 1000;
    private final String[] buffer = new String[MAX_SIZE];
    private final AtomicInteger index = new AtomicInteger(0);
    private final AtomicInteger count = new AtomicInteger(0);

    // 실시간 로그 구독자들
    private final List<Consumer<String>> subscribers = new CopyOnWriteArrayList<>();

    /**
     * 로그 추가
     */
    public void append(String logMessage) {
        int currentIndex = index.getAndIncrement() % MAX_SIZE;
        buffer[currentIndex] = logMessage;
        count.incrementAndGet();

        // 구독자들에게 실시간 전송
        notifySubscribers(logMessage);
    }

    /**
     * 버퍼에 저장된 모든 로그 조회 (시간순)
     */
    public List<String> getAllLogs() {
        List<String> logs = new ArrayList<>();
        int total = Math.min(count.get(), MAX_SIZE);
        int currentIndex = index.get();

        // 가장 오래된 로그부터 순서대로 조회
        for (int i = 0; i < total; i++) {
            int pos = (currentIndex - total + i + MAX_SIZE) % MAX_SIZE;
            if (buffer[pos] != null) {
                logs.add(buffer[pos]);
            }
        }

        return logs;
    }

    /**
     * 실시간 로그 구독
     */
    public void subscribe(Consumer<String> subscriber) {
        subscribers.add(subscriber);
    }

    /**
     * 구독 해제
     */
    public void unsubscribe(Consumer<String> subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * 구독자들에게 로그 전송
     */
    private void notifySubscribers(String logMessage) {
        for (Consumer<String> subscriber : subscribers) {
            try {
                subscriber.accept(logMessage);
            } catch (Exception e) {
                // 구독자 전송 실패 시 무시 (연결 끊김 등)
            }
        }
    }
}
