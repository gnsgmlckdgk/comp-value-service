package com.finance.dart.cointrade.service;

import com.finance.dart.cointrade.consts.CoinTraderProgramConfig;
import com.finance.dart.cointrade.dto.*;
import com.finance.dart.common.component.HttpClientComponent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 백테스트 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CointradeBacktestService {

    private final HttpClientComponent httpClientComponent;

    @Value("${app.local}")
    private boolean isLocal;

    /**
     * 백테스트 실행
     */
    public BacktestRunResDto runBacktest(BacktestRunReqDto request) {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_BACKTEST_RUN);
        log.info("백테스트 실행 요청 - URL: {}, request: {}", url, request);

        return httpClientComponent.exchangeSync(
                url,
                HttpMethod.POST,
                null,
                request,
                new ParameterizedTypeReference<BacktestRunResDto>() {}
        ).getBody();
    }

    /**
     * 백테스트 작업 상태 조회
     */
    public BacktestStatusDto getStatus(String taskId) {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_BACKTEST_STATUS + "/" + taskId);
        log.info("백테스트 작업 상태 조회 요청 - URL: {}, taskId: {}", url, taskId);

        return httpClientComponent.exchangeSync(
                url,
                HttpMethod.GET,
                new ParameterizedTypeReference<BacktestStatusDto>() {}
        ).getBody();
    }

    /**
     * 백테스트 결과 조회
     */
    public BacktestResultDto getResult(String taskId, Boolean includeIndividual, Boolean includeTrades) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(buildUrl(CoinTraderProgramConfig.API_URI_BACKTEST_RESULT + "/" + taskId));

        boolean hasParam = false;
        if (includeIndividual != null && includeIndividual) {
            urlBuilder.append("?include_individual=true");
            hasParam = true;
        }

        if (includeTrades != null && includeTrades) {
            if (hasParam) {
                urlBuilder.append("&include_trades=true");
            } else {
                urlBuilder.append("?include_trades=true");
            }
        }

        String url = urlBuilder.toString();
        log.info("백테스트 결과 조회 요청 - URL: {}, taskId: {}", url, taskId);

        return httpClientComponent.exchangeSync(
                url,
                HttpMethod.GET,
                new ParameterizedTypeReference<BacktestResultDto>() {}
        ).getBody();
    }

    /**
     * 백테스트 결과 삭제
     */
    public BacktestDeleteResDto deleteResult(String taskId) {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_BACKTEST_RESULT + "/" + taskId);
        log.info("백테스트 결과 삭제 요청 - URL: {}, taskId: {}", url, taskId);

        return httpClientComponent.exchangeSync(
                url,
                HttpMethod.DELETE,
                new ParameterizedTypeReference<BacktestDeleteResDto>() {}
        ).getBody();
    }

    /**
     * 백테스트 이력 조회
     */
    public List<BacktestHistoryDto> getHistory() {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_BACKTEST_HISTORY);
        log.info("백테스트 이력 조회 요청 - URL: {}", url);

        return httpClientComponent.exchangeSync(
                url,
                HttpMethod.GET,
                new ParameterizedTypeReference<List<BacktestHistoryDto>>() {}
        ).getBody();
    }

    /**
     * URL 생성 헬퍼 메서드
     */
    private String buildUrl(String uri) {
        String baseUrl = isLocal
                ? CoinTraderProgramConfig.localHost
                : CoinTraderProgramConfig.prodHost;

        return baseUrl + "/" + uri;
    }
}
