package com.finance.dart.monitoring.controller;

import com.finance.dart.common.config.EndPointConfig;
import com.finance.dart.common.logging.TransactionLogging;
import com.finance.dart.member.enums.RoleConstants;
import com.finance.dart.monitoring.buffer.MonitoringEventBuffer;
import com.finance.dart.monitoring.dto.ApiRequestLogDto;
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

import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final com.finance.dart.monitoring.tracker.DownstreamTrafficTracker downstreamTrafficTracker;
    private final com.finance.dart.monitoring.tracker.RequestTrafficTracker requestTrafficTracker;

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

            // 4. 실시간 traffic 구독 (1초 간격, 서비스별 개별 카운트)
            Consumer<Map<String, Integer>> trafficSubscriber = trafficData -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("traffic")
                            .data(objectMapper.writeValueAsString(trafficData))
                            .build());
                } catch (IOException e) {
                    log.debug("traffic 전송 실패 (클라이언트 연결 끊김)");
                    emitter.completeWithError(e);
                }
            };

            // 5. 실시간 api-log 구독 (1초 간격, 개별 API 요청 로그 배치)
            Consumer<List<ApiRequestLogDto>> apiLogSubscriber = logs -> {
                try {
                    emitter.send(SseEmitter.event()
                            .name("api-log")
                            .data(objectMapper.writeValueAsString(logs))
                            .build());
                } catch (IOException e) {
                    log.debug("api-log 전송 실패 (클라이언트 연결 끊김)");
                    emitter.completeWithError(e);
                }
            };

            eventBuffer.subscribeSnapshot(snapshotSubscriber);
            eventBuffer.subscribeTrade(tradeSubscriber);
            eventBuffer.subscribeTraffic(trafficSubscriber);
            eventBuffer.subscribeApiLog(apiLogSubscriber);

            // 6. 연결 종료 시 구독 해제
            Runnable cleanup = () -> {
                eventBuffer.unsubscribeSnapshot(snapshotSubscriber);
                eventBuffer.unsubscribeTrade(tradeSubscriber);
                eventBuffer.unsubscribeTraffic(trafficSubscriber);
                eventBuffer.unsubscribeApiLog(apiLogSubscriber);
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

    /**
     * 디버그용: 현재 snapshot을 JSON으로 반환
     * curl http://localhost:18080/dart/monitoring/snapshot
     */
    @EndPointConfig.PublicEndpoint
    @GetMapping("/snapshot")
    public ResponseEntity<?> snapshot() {
        MonitoringSnapshotDto latest = eventBuffer.getLatestSnapshot();
        if (latest == null) {
            return ResponseEntity.ok(Map.of("status", "NO_DATA", "message", "아직 snapshot이 수집되지 않았습니다."));
        }
        return ResponseEntity.ok(latest);
    }

    /**
     * 디버그용: 현재 트래픽 카운터 조회 (리셋 없이)
     * curl http://localhost:18080/dart/monitoring/traffic-debug
     */
    @EndPointConfig.PublicEndpoint
    @GetMapping("/traffic-debug")
    public ResponseEntity<?> trafficDebug() {
        return ResponseEntity.ok(Map.of(
                "http_pending", requestTrafficTracker.get(),
                "downstream_pending", downstreamTrafficTracker.peekAll(),
                "isInUserRequest", com.finance.dart.monitoring.tracker.RequestTrafficTracker.isInUserRequest()
        ));
    }

    /**
     * 테스트용: 가짜 거래 이벤트 발사 → SSE로 전송됨
     * curl http://localhost:18080/dart/monitoring/test-trade
     */
    @EndPointConfig.PublicEndpoint
    @GetMapping("/test-trade")
    public ResponseEntity<?> testTrade() {
        TradeEventDto fakeTrade = TradeEventDto.builder()
                .id(System.currentTimeMillis())
                .coinCode("BTC")
                .tradeType("BUY")
                .price(new BigDecimal("95000000"))
                .quantity(new BigDecimal("0.001"))
                .totalAmount(new BigDecimal("95000"))
                .profitLoss(BigDecimal.ZERO)
                .profitLossRate(BigDecimal.ZERO)
                .reason("테스트 거래")
                .timestamp(LocalDateTime.now())
                .build();

        eventBuffer.publishTrade(fakeTrade);
        log.info("테스트 거래 이벤트 발사: {}", fakeTrade.getCoinCode());

        return ResponseEntity.ok(Map.of("status", "OK", "message", "테스트 거래 이벤트 발사됨", "trade", fakeTrade));
    }
}
