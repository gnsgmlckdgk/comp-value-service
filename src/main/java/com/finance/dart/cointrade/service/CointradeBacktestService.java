package com.finance.dart.cointrade.service;

import com.finance.dart.cointrade.consts.CoinTraderProgramConfig;
import com.finance.dart.cointrade.dto.*;
import com.finance.dart.common.component.HttpClientComponent;
import com.finance.dart.common.util.ConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

        Map<String, Object> requestParam = new LinkedHashMap<>();
        requestParam.put("coin_codes", request.getCoinCodes());
        requestParam.put("start_date", request.getStartDate());
        requestParam.put("end_date", request.getEndDate());

        if(request.getConfig() != null) {
            Map<String, Object> config = ConvertUtil.parseObject(request.getConfig(), Map.class);
            requestParam.putAll(config);
        }

        return httpClientComponent.exchangeSync(
                url,
                HttpMethod.POST,
                null,
                requestParam,
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
     * 백테스트 작업 취소
     */
    public BacktestDeleteResDto cancelBacktest(String taskId) {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_BACKTEST_CANCEL + "/" + taskId);
        log.info("백테스트 작업 취소 요청 - URL: {}, taskId: {}", url, taskId);

        return httpClientComponent.exchangeSync(
                url,
                HttpMethod.POST,
                new ParameterizedTypeReference<BacktestDeleteResDto>() {}
        ).getBody();
    }

    /**
     * 백테스트 옵티마이저 실행
     */
    public OptimizerRunResDto runOptimizer(OptimizerRunReqDto request) {

        String url = buildUrl(CoinTraderProgramConfig.API_URI_OPTIMIZER_RUN);
        log.info("백테스트 옵티마이저 실행 요청 - URL: {}, request: {}", url, request);

        Map<String, Object> requestParam = new LinkedHashMap<>();
        requestParam.put("coin_codes", request.getCoinCodes());
        requestParam.put("start_date", request.getStartDate());
        requestParam.put("end_date", request.getEndDate());

        if (request.getMaxRuns() != null) {
            requestParam.put("max_runs", request.getMaxRuns());
        }
        if (request.getMaxTimeMinutes() != null) {
            requestParam.put("max_time_minutes", request.getMaxTimeMinutes());
        }
        if (request.getTargetReturn() != null) {
            requestParam.put("target_return", request.getTargetReturn());
        }
        if (request.getNumWorkers() != null) {
            requestParam.put("num_workers", request.getNumWorkers());
        }
        if (request.getCustomParamRanges() != null) {
            requestParam.put("custom_param_ranges", request.getCustomParamRanges());
        }

        return httpClientComponent.exchangeSync(
                url,
                HttpMethod.POST,
                null,
                requestParam,
                new ParameterizedTypeReference<OptimizerRunResDto>() {}
        ).getBody();
    }

    /**
     * 백테스트 옵티마이저 상태 조회
     */
    public OptimizerStatusDto getOptimizerStatus(String taskId) {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_OPTIMIZER_STATUS + "/" + taskId);
        log.info("백테스트 옵티마이저 상태 조회 요청 - URL: {}, taskId: {}", url, taskId);

        return httpClientComponent.exchangeSync(
                url,
                HttpMethod.GET,
                new ParameterizedTypeReference<OptimizerStatusDto>() {}
        ).getBody();
    }

    /**
     * 백테스트 옵티마이저 결과 조회
     */
    public OptimizerResultDto getOptimizerResult(String taskId, Boolean includeAllTrials) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(buildUrl(CoinTraderProgramConfig.API_URI_OPTIMIZER_RESULT + "/" + taskId));

        if (includeAllTrials != null && includeAllTrials) {
            urlBuilder.append("?include_all_trials=true");
        }

        String url = urlBuilder.toString();
        log.info("백테스트 옵티마이저 결과 조회 요청 - URL: {}, taskId: {}", url, taskId);

        return httpClientComponent.exchangeSync(
                url,
                HttpMethod.GET,
                new ParameterizedTypeReference<OptimizerResultDto>() {}
        ).getBody();
    }

    /**
     * 백테스트 옵티마이저 이력 조회
     */
    public List<OptimizerHistoryDto> getOptimizerHistory(Integer limit) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(buildUrl(CoinTraderProgramConfig.API_URI_OPTIMIZER_HISTORY));

        if (limit != null) {
            urlBuilder.append("?limit=").append(limit);
        }

        String url = urlBuilder.toString();
        log.info("백테스트 옵티마이저 이력 조회 요청 - URL: {}", url);

        return httpClientComponent.exchangeSync(
                url,
                HttpMethod.GET,
                new ParameterizedTypeReference<List<OptimizerHistoryDto>>() {}
        ).getBody();
    }

    /**
     * 백테스트 옵티마이저 작업 취소
     */
    public BacktestDeleteResDto cancelOptimizer(String taskId) {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_OPTIMIZER_CANCEL + "/" + taskId);
        log.info("백테스트 옵티마이저 작업 취소 요청 - URL: {}, taskId: {}", url, taskId);

        return httpClientComponent.exchangeSync(
                url,
                HttpMethod.POST,
                new ParameterizedTypeReference<BacktestDeleteResDto>() {}
        ).getBody();
    }

    /**
     * 백테스트 옵티마이저 결과 삭제
     */
    public BacktestDeleteResDto deleteOptimizerResult(String taskId) {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_OPTIMIZER_DELETE + "/" + taskId);
        log.info("백테스트 옵티마이저 결과 삭제 요청 - URL: {}, taskId: {}", url, taskId);

        return httpClientComponent.exchangeSync(
                url,
                HttpMethod.DELETE,
                new ParameterizedTypeReference<BacktestDeleteResDto>() {}
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
