package com.finance.dart.monitoring.service;

import com.finance.dart.common.component.ConfigComponent;
import com.finance.dart.monitoring.dto.ServiceStatusDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.http.HttpStatusCode;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 외부 API 헬스체크 서비스
 * - FMP, DART, Upbit, OpenAI, SEC 각각 경량 요청으로 UP/DOWN + 응답시간 측정
 * - 30초 주기로 호출 (rate limit 부담 최소화)
 */
@Slf4j
@Service
public class ExternalApiHealthService {

    private final WebClient webClient;
    private final ConfigComponent configComponent;

    /** 캐시된 결과 — 30초 주기로 갱신 */
    private volatile List<ServiceStatusDto> cachedStatus = List.of();

    public ExternalApiHealthService(WebClient webClient, ConfigComponent configComponent) {
        this.webClient = webClient;
        this.configComponent = configComponent;
    }

    public List<ServiceStatusDto> getCachedStatus() {
        return cachedStatus;
    }

    /**
     * 전체 외부 API 헬스체크 실행 및 캐시 갱신
     */
    public void refreshAll() {
        List<ServiceStatusDto> results = new ArrayList<>();
        results.add(checkFmp());
        results.add(checkDart());
        results.add(checkUpbit());
        results.add(checkOpenAi());
        results.add(checkSec());
        cachedStatus = results;
    }

    private ServiceStatusDto checkFmp() {
        String apiKey = configComponent.getFmpApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return ServiceStatusDto.builder().name("FMP").status("DOWN").version("No API Key").build();
        }
        return timedCheck("FMP",
                "https://financialmodelingprep.com/stable/profile?symbol=AAPL&apikey=" + apiKey);
    }

    private ServiceStatusDto checkDart() {
        String apiKey = configComponent.getDartApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return ServiceStatusDto.builder().name("DART").status("DOWN").version("No API Key").build();
        }
        // corpCode.xml은 가장 가벼운 DART 엔드포인트 (응답 존재 여부만 확인)
        return timedCheck("DART",
                "https://opendart.fss.or.kr/api/corpCode.xml?crtfc_key=" + apiKey);
    }

    private ServiceStatusDto checkUpbit() {
        return timedCheck("Upbit", "https://api.upbit.com/v1/market/all");
    }

    private ServiceStatusDto checkOpenAi() {
        String apiKey = configComponent.getOpenAiApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            return ServiceStatusDto.builder().name("OpenAI").status("DOWN").version("No API Key").build();
        }
        long start = System.currentTimeMillis();
        try {
            String response = webClient.get()
                    .uri("https://api.openai.com/v1/models")
                    .header("Authorization", "Bearer " + apiKey)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> Mono.empty())
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            long elapsed = System.currentTimeMillis() - start;
            if (response != null) {
                return ServiceStatusDto.builder().name("OpenAI").status("UP")
                        .uptime(elapsed + "ms").build();
            }
        } catch (Exception e) {
            log.debug("OpenAI 헬스체크 실패: {}", e.getMessage());
        }
        long elapsed = System.currentTimeMillis() - start;
        return ServiceStatusDto.builder().name("OpenAI").status("DOWN")
                .uptime(elapsed + "ms").build();
    }

    private ServiceStatusDto checkSec() {
        // SEC API는 User-Agent 헤더 필수
        long start = System.currentTimeMillis();
        try {
            String response = webClient.get()
                    .uri("https://data.sec.gov/api/xbrl/companyfacts/CIK0000320193.json")
                    .header("User-Agent", "MyFinanceTool/1.0 (contact: dohauzi@gmail.com)")
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> Mono.empty())
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            long elapsed = System.currentTimeMillis() - start;
            if (response != null) {
                return ServiceStatusDto.builder().name("SEC").status("UP")
                        .uptime(elapsed + "ms").build();
            }
        } catch (Exception e) {
            log.debug("SEC 헬스체크 실패: {}", e.getMessage());
        }
        long elapsed = System.currentTimeMillis() - start;
        return ServiceStatusDto.builder().name("SEC").status("DOWN")
                .uptime(elapsed + "ms").build();
    }

    /**
     * 공통: URL에 GET 요청 보내서 응답 시간 측정
     */
    private ServiceStatusDto timedCheck(String name, String url) {
        long start = System.currentTimeMillis();
        try {
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, resp -> Mono.empty())
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            long elapsed = System.currentTimeMillis() - start;
            if (response != null) {
                return ServiceStatusDto.builder().name(name).status("UP")
                        .uptime(elapsed + "ms").build();
            }
        } catch (Exception e) {
            log.debug("{} 헬스체크 실패: {}", name, e.getMessage());
        }
        long elapsed = System.currentTimeMillis() - start;
        return ServiceStatusDto.builder().name(name).status("DOWN")
                .uptime(elapsed + "ms").build();
    }
}
