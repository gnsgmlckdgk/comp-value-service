package com.finance.dart.monitoring.controller;

import com.finance.dart.common.config.EndPointConfig;
import com.finance.dart.common.logging.TransactionLogging;
import com.finance.dart.member.enums.RoleConstants;
import com.finance.dart.monitoring.buffer.MonitoringEventBuffer;
import com.finance.dart.monitoring.dto.MonitoringSnapshotDto;
import com.finance.dart.monitoring.dto.TradeEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * 모니터링 SSE 스트리밍 엔드포인트
 * ManagementController.java SSE 패턴 복제 — named events 사용
 */
@Slf4j
@RestController
@RequestMapping("monitoring")
@RequiredArgsConstructor
public class MonitoringController {

    private final MonitoringEventBuffer eventBuffer;
    private final ObjectMapper objectMapper;

    /**
     * SSE 스트림 엔드포인트
     * named events: "snapshot", "trade"
     *
     * 사용법: curl http://localhost:18080/dart/monitoring/stream
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        log.info("모니터링 SSE 스트리밍 시작 - 클라이언트 연결됨");

        try {
            // 1. 최신 snapshot이 있으면 즉시 전송
            MonitoringSnapshotDto latestSnapshot = eventBuffer.getLatestSnapshot();
            if (latestSnapshot != null) {
                emitter.send(SseEmitter.event()
                        .name("snapshot")
                        .data(objectMapper.writeValueAsString(latestSnapshot))
                        .build());
            }

            // 2. 실시간 snapshot 구독
            Consumer<MonitoringSnapshotDto> snapshotSubscriber = snapshot -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("snapshot")
                            .data(objectMapper.writeValueAsString(snapshot))
                            .build());
                } catch (IOException e) {
                    log.debug("snapshot 전송 실패 (클라이언트 연결 끊김)");
                    emitter.completeWithError(e);
                }
            };

            // 3. 실시간 trade 구독
            Consumer<TradeEventDto> tradeSubscriber = trade -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("trade")
                            .data(objectMapper.writeValueAsString(trade))
                            .build());
                } catch (IOException e) {
                    log.debug("trade 전송 실패 (클라이언트 연결 끊김)");
                    emitter.completeWithError(e);
                }
            };

            eventBuffer.subscribeSnapshot(snapshotSubscriber);
            eventBuffer.subscribeTrade(tradeSubscriber);

            // 4. 연결 종료 시 구독 해제
            Runnable cleanup = () -> {
                eventBuffer.unsubscribeSnapshot(snapshotSubscriber);
                eventBuffer.unsubscribeTrade(tradeSubscriber);
            };

            emitter.onCompletion(() -> {
                log.info("모니터링 SSE 스트리밍 종료 - 클라이언트 연결 정상 종료");
                cleanup.run();
            });

            emitter.onTimeout(() -> {
                log.warn("모니터링 SSE 스트리밍 타임아웃");
                cleanup.run();
                emitter.complete();
            });

            emitter.onError(ex -> {
                log.error("모니터링 SSE 스트리밍 에러: {}", ex.getMessage());
                cleanup.run();
            });

        } catch (Exception e) {
            log.error("모니터링 SSE 스트리밍 초기화 실패", e);
            emitter.completeWithError(e);
        }

        return emitter;
    }
}
