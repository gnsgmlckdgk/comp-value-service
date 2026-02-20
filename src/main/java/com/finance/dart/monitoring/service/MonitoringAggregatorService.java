package com.finance.dart.monitoring.service;

import com.finance.dart.monitoring.buffer.MonitoringEventBuffer;
import com.finance.dart.monitoring.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * 모니터링 데이터 집계 오케스트레이터
 * 각 소스를 주기적으로 폴링하여 MonitoringEventBuffer로 publish
 *
 * snapshot publish는 aggregateSnapshot() 한 곳에서만 수행.
 * resources/trades는 별도 주기로 캐시만 갱신하고, 다음 snapshot에 포함됨.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringAggregatorService {

    private final CointraderStatusService cointraderStatusService;
    private final TradeEventDetectorService tradeEventDetectorService;
    private final PrometheusQueryService prometheusQueryService;
    private final MonitoringEventBuffer eventBuffer;

    /** 리소스 메트릭 캐시 — aggregateResources()에서 갱신, aggregateSnapshot()에서 읽기 */
    private volatile ResourceMetricsDto cachedResources = null;

    /**
     * 3초 주기: 전체 snapshot 생성 및 publish (유일한 publish 포인트)
     * 개별 서비스 헬스체크 실패가 전체 snapshot 발행을 막지 않도록 각각 try-catch
     */
    @Scheduled(fixedDelay = 3000)
    public void aggregateSnapshot() {
        try {
            // 서비스 상태 수집 (6개: web, backend, cointrader, stock-predictor, postgresql, redis)
            List<ServiceStatusDto> services = new ArrayList<>();
            services.add(safeGetHealth("comp-value-admin", () -> cointraderStatusService.getWebHealth()));
            services.add(getBackendStatus());
            services.add(safeGetHealth("cointrader", () -> cointraderStatusService.getCointraderHealth()));
            services.add(safeGetHealth("stock-predictor", () -> cointraderStatusService.getStockPredictorHealth()));
            services.add(safeGetHealth("postgresql", () -> cointraderStatusService.getPostgresHealth()));
            services.add(safeGetHealth("redis", () -> cointraderStatusService.getRedisHealth()));

            log.debug("모니터링 서비스 수집 완료: {}개 [{}]", services.size(),
                    services.stream()
                            .map(s -> s.getName() + "=" + s.getStatus())
                            .reduce((a, b) -> a + ", " + b).orElse(""));

            // 프로세스 상태
            ProcessStatusDto buyProcess = safeGetProcess("buy");
            ProcessStatusDto sellProcess = safeGetProcess("sell");

            // 오늘 거래 건수
            int todayTradeCount = safeGetTodayTradeCount();

            MonitoringSnapshotDto snapshot = MonitoringSnapshotDto.builder()
                    .services(services)
                    .buyProcess(buyProcess)
                    .sellProcess(sellProcess)
                    .resources(cachedResources)
                    .holdingsCount(0)
                    .todayTradeCount(todayTradeCount)
                    .timestamp(System.currentTimeMillis())
                    .build();

            eventBuffer.publishSnapshot(snapshot);

        } catch (Exception e) {
            log.warn("모니터링 snapshot 수집 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 10초 주기: Prometheus 리소스 메트릭 수집 → 캐시만 갱신 (publish하지 않음)
     */
    @Scheduled(fixedDelay = 10000)
    public void aggregateResources() {
        try {
            cachedResources = prometheusQueryService.queryMetrics();
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

    /** 개별 헬스체크를 안전하게 호출 — 어떤 예외든 잡아서 DOWN 반환 */
    private ServiceStatusDto safeGetHealth(String serviceName, Supplier<ServiceStatusDto> supplier) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("헬스체크 실패 [{}]: {}", serviceName, e.getMessage());
            return ServiceStatusDto.builder().name(serviceName).status("DOWN").build();
        }
    }

    private ProcessStatusDto safeGetProcess(String processName) {
        try {
            return cointraderStatusService.getProcessStatus(processName);
        } catch (Exception e) {
            log.debug("프로세스 상태 조회 실패 [{}]: {}", processName, e.getMessage());
            return ProcessStatusDto.builder().name(processName).status("IDLE").percent(0).message("").build();
        }
    }

    private int safeGetTodayTradeCount() {
        try {
            return tradeEventDetectorService.getTodayTradeCount();
        } catch (Exception e) {
            log.debug("오늘 거래 건수 조회 실패: {}", e.getMessage());
            return 0;
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
