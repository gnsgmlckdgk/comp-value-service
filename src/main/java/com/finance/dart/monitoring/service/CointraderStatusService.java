package com.finance.dart.monitoring.service;

import com.finance.dart.monitoring.dto.ProcessStatusDto;
import com.finance.dart.monitoring.dto.ServiceStatusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

/**
 * cointrader 서비스 상태 폴링
 * /api/process/status, /api/health 엔드포인트 조회
 */
@Slf4j
@Service
public class CointraderStatusService {

    private final WebClient webClient;
    private final String cointraderUrl;

    public CointraderStatusService(
            WebClient webClient,
            @Value("${monitoring.cointrader.url:http://localhost:18082}") String cointraderUrl
    ) {
        this.webClient = webClient;
        this.cointraderUrl = cointraderUrl;
    }

    /**
     * cointrader 헬스 체크
     */
    @SuppressWarnings("unchecked")
    public ServiceStatusDto getHealth() {
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

        return ServiceStatusDto.builder()
                .name("cointrader")
                .status("DOWN")
                .uptime("")
                .version("")
                .build();
    }

    /**
     * 매수/매도 프로세스 상태 조회
     */
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

        return ProcessStatusDto.builder()
                .name(processName)
                .status("IDLE")
                .percent(0)
                .message("")
                .build();
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
