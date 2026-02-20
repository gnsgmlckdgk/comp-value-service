package com.finance.dart.monitoring.service;

import com.finance.dart.monitoring.buffer.MonitoringEventBuffer;
import com.finance.dart.monitoring.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 모니터링 데이터 집계 오케스트레이터
 * 각 소스를 주기적으로 폴링하여 MonitoringEventBuffer로 publish
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringAggregatorService {

    private final CointraderStatusService cointraderStatusService;
    private final TradeEventDetectorService tradeEventDetectorService;
    private final PrometheusQueryService prometheusQueryService;
    private final MonitoringEventBuffer eventBuffer;

    /**
     * 3초 주기: 서비스 상태 + 프로세스 상태 snapshot 생성
     */
    @Scheduled(fixedDelay = 3000)
    public void aggregateSnapshot() {
        try {
            // 서비스 상태 수집
            List<ServiceStatusDto> services = new ArrayList<>();
            services.add(getBackendStatus());
            services.add(cointraderStatusService.getHealth());

            // 프로세스 상태
            ProcessStatusDto buyProcess = cointraderStatusService.getProcessStatus("buy");
            ProcessStatusDto sellProcess = cointraderStatusService.getProcessStatus("sell");

            // 오늘 거래 건수
            int todayTradeCount = tradeEventDetectorService.getTodayTradeCount();

            // 이전 snapshot의 resources를 보존 (10초 주기로만 갱신되므로)
            MonitoringSnapshotDto prev = eventBuffer.getLatestSnapshot();
            ResourceMetricsDto prevResources = prev != null ? prev.getResources() : null;

            MonitoringSnapshotDto snapshot = MonitoringSnapshotDto.builder()
                    .services(services)
                    .buyProcess(buyProcess)
                    .sellProcess(sellProcess)
                    .resources(prevResources)
                    .holdingsCount(0) // TODO: 보유량 서비스 연동
                    .todayTradeCount(todayTradeCount)
                    .timestamp(System.currentTimeMillis())
                    .build();

            eventBuffer.publishSnapshot(snapshot);

        } catch (Exception e) {
            log.warn("모니터링 snapshot 수집 실패: {}", e.getMessage());
        }
    }

    /**
     * 10초 주기: Prometheus 리소스 메트릭 수집 → snapshot에 포함
     */
    @Scheduled(fixedDelay = 10000)
    public void aggregateResources() {
        try {
            ResourceMetricsDto metrics = prometheusQueryService.queryMetrics();
            MonitoringSnapshotDto latest = eventBuffer.getLatestSnapshot();
            if (latest != null) {
                latest.setResources(metrics);
                eventBuffer.publishSnapshot(latest);
            }
        } catch (Exception e) {
            log.debug("Prometheus 메트릭 수집 실패: {}", e.getMessage());
        }
    }

    /**
     * 5초 주기: DB에서 신규 거래 감지
     */
    @Scheduled(fixedDelay = 5000)
    public void detectTrades() {
        try {
            List<TradeEventDto> newTrades = tradeEventDetectorService.detectNewTrades();
            for (TradeEventDto trade : newTrades) {
                eventBuffer.publishTrade(trade);
            }
        } catch (Exception e) {
            log.debug("거래 감지 실패: {}", e.getMessage());
        }
    }

    private ServiceStatusDto getBackendStatus() {
        long uptimeMs = System.currentTimeMillis() - startTime;
        long hours = uptimeMs / 3600000;
        long minutes = (uptimeMs % 3600000) / 60000;

        return ServiceStatusDto.builder()
                .name("comp-value-service")
                .status("UP")
                .uptime(hours + "h " + minutes + "m")
                .build();
    }

    private static final long startTime = System.currentTimeMillis();
}
