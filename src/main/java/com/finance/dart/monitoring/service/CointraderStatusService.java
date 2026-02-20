package com.finance.dart.monitoring.service;

import com.finance.dart.monitoring.dto.ProcessStatusDto;
import com.finance.dart.monitoring.dto.ServiceStatusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.http.HttpStatusCode;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;
import java.util.Map;

/**
 * 전체 서비스 헬스체크
 * - cointrader: HTTP /api/health, /api/process/status
 * - stock-predictor (ML): HTTP /
 * - comp-value-admin (Web): HTTP /
 * - PostgreSQL: JDBC connection test
 * - Redis: ping
 */
@Slf4j
@Service
public class CointraderStatusService {

    private final WebClient webClient;
    private final String cointraderUrl;
    private final String stockPredictorUrl;
    private final String webUrl;
    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    public CointraderStatusService(
            WebClient webClient,
            @Value("${monitoring.cointrader.url:http://localhost:18082}") String cointraderUrl,
            @Value("${monitoring.stock-predictor.url:http://localhost:18081}") String stockPredictorUrl,
            @Value("${monitoring.web.url:http://localhost:80}") String webUrl,
            DataSource dataSource,
            RedisConnectionFactory redisConnectionFactory
    ) {
        this.webClient = webClient;
        this.cointraderUrl = cointraderUrl;
        this.stockPredictorUrl = stockPredictorUrl;
        this.webUrl = webUrl;
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    // === Cointrader ===

    @SuppressWarnings("unchecked")
    public ServiceStatusDto getCointraderHealth() {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(cointraderUrl + "/api/health")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            if (response != null) {
                String status = String.valueOf(response.getOrDefault("status", "UNKNOWN"));
                String uptime = String.valueOf(response.getOrDefault("uptime", ""));
                String version = String.valueOf(response.getOrDefault("version", ""));

                return ServiceStatusDto.builder()
                        .name("cointrader")
                        .status("ok".equalsIgnoreCase(status) || "healthy".equalsIgnoreCase(status) ? "UP" : "DEGRADED")
                        .uptime(uptime)
                        .version(version)
                        .build();
            }
        } catch (Exception e) {
            log.debug("cointrader 헬스체크 실패: {}", e.getMessage());
        }

        return ServiceStatusDto.builder().name("cointrader").status("DOWN").build();
    }

    @SuppressWarnings("unchecked")
    public ProcessStatusDto getProcessStatus(String processName) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(cointraderUrl + "/api/process/status")
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            if (response != null) {
                Map<String, Object> process = (Map<String, Object>) response.get(processName);
                if (process != null) {
                    return ProcessStatusDto.builder()
                            .name(processName)
                            .status(String.valueOf(process.getOrDefault("status", "IDLE")))
                            .percent(toInt(process.get("percent")))
                            .message(String.valueOf(process.getOrDefault("message", "")))
                            .build();
                }
            }
        } catch (Exception e) {
            log.debug("cointrader 프로세스 상태 조회 실패 [{}]: {}", processName, e.getMessage());
        }

        return ProcessStatusDto.builder().name(processName).status("IDLE").percent(0).message("").build();
    }

    // === Stock Predictor (ML) ===

    public ServiceStatusDto getStockPredictorHealth() {
        try {
            // ML 서비스에는 / 경로가 없으므로 /logs 사용 (가볍고 항상 200 반환)
            // 4xx/5xx도 서비스가 살아있다는 뜻이므로 onStatus로 에러 무시
            String response = webClient.get()
                    .uri(stockPredictorUrl + "/logs")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> Mono.empty())
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            // response가 null이 아니면 서비스가 응답한 것 → UP
            if (response != null) {
                return ServiceStatusDto.builder().name("stock-predictor").status("UP").build();
            }
        } catch (Exception e) {
            log.debug("stock-predictor 헬스체크 실패: {}", e.getMessage());
        }

        return ServiceStatusDto.builder().name("stock-predictor").status("DOWN").build();
    }

    // === Web (Nginx / Vite dev) ===

    public ServiceStatusDto getWebHealth() {
        try {
            // 4xx도 서비스가 살아있다는 뜻이므로 에러 무시
            String response = webClient.get()
                    .uri(webUrl + "/")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> Mono.empty())
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(3))
                    .block();

            if (response != null) {
                return ServiceStatusDto.builder().name("comp-value-admin").status("UP").build();
            }
        } catch (Exception e) {
            log.debug("web 헬스체크 실패: {}", e.getMessage());
        }

        return ServiceStatusDto.builder().name("comp-value-admin").status("DOWN").build();
    }

    // === PostgreSQL ===

    public ServiceStatusDto getPostgresHealth() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(2)) {
                return ServiceStatusDto.builder().name("postgresql").status("UP").build();
            }
        } catch (Exception e) {
            log.debug("PostgreSQL 헬스체크 실패: {}", e.getMessage());
        }

        return ServiceStatusDto.builder().name("postgresql").status("DOWN").build();
    }

    // === Redis ===

    public ServiceStatusDto getRedisHealth() {
        try {
            org.springframework.data.redis.connection.RedisConnection conn = redisConnectionFactory.getConnection();
            String pong = conn.ping();
            conn.close();
            if ("PONG".equalsIgnoreCase(pong)) {
                return ServiceStatusDto.builder().name("redis").status("UP").build();
            }
        } catch (Exception e) {
            log.debug("Redis 헬스체크 실패: {}", e.getMessage());
        }

        return ServiceStatusDto.builder().name("redis").status("DOWN").build();
    }

    private int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) return ((Number) value).intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
