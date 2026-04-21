package com.finance.dart.cointrade.service;

import com.finance.dart.cointrade.consts.CoinTraderProgramConfig;
import com.finance.dart.cointrade.dto.*;
import com.finance.dart.cointrade.dto.upbit.TradingParisDto;
import com.finance.dart.cointrade.entity.CointradeTargetCoinEntity;
import com.finance.dart.cointrade.entity.CointradeTradeHistoryEntity;
import com.finance.dart.cointrade.repository.CointradeConfigRepository;
import com.finance.dart.cointrade.repository.CointradeHoldingRepository;
import com.finance.dart.cointrade.repository.CointradeTargetCoinRepository;
import com.finance.dart.cointrade.repository.CointradeTradeHistoryRepository;
import com.finance.dart.common.component.HttpClientComponent;
import com.finance.dart.common.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 코인 자동매매 설정 관리 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CointradeConfigService {

    private final CointradeConfigRepository configRepository;
    private final CointradeTargetCoinRepository targetCoinRepository;
    private final CointradeHoldingRepository holdingRepository;
    private final CointradeTradeHistoryRepository tradeHistoryRepository;
    private final UpbitService upbitService;

    private final HttpClientComponent httpClientComponent;

    @Value("${app.local}")
    private boolean isLocal;


    /**
     * 전체 설정값 조회
     */
    @Transactional(readOnly = true)
    public List<CointradeConfigDto> getAllConfigs() {
        return configRepository.findAll().stream()
                .map(entity -> CointradeConfigDto.builder()
                        .paramName(entity.getParamName())
                        .paramValue(entity.getParamValue())
                        .description(entity.getDescription())
                        .updatedAt(entity.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 설정값 일괄 수정
     */
    @Transactional
    public void updateConfigs(List<CointradeConfigUpdateDto> configs) {
        for (CointradeConfigUpdateDto dto : configs) {
            configRepository.findByParamName(dto.getConfigKey())
                    .ifPresent(entity -> {
                        entity.setParamValue(dto.getConfigValue());
                        configRepository.save(entity);
                    });
        }
        log.info("설정값 {}개 수정 완료", configs.size());
    }

    /**
     * 대상 종목 목록 조회 (Upbit 동기화 후 DB 조회)
     */
    @Transactional
    public List<CointradeTargetCoinDto> getAllTargetCoins() {
        syncUpbitMarkets();
        return targetCoinRepository.findByUseYn("Y").stream()
                .map(entity -> CointradeTargetCoinDto.builder()
                        .coinCode(entity.getCoinCode())
                        .coinName(entity.getCoinName())
                        .isActive(entity.getIsActive())
                        .useYn(entity.getUseYn())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 대상 종목 설정 (선택한 종목들 is_active = true, 나머지 false)
     * Upbit 동기화 후 is_active 설정
     */
    @Transactional
    public void updateTargetCoins(List<String> coinCodes) {
        syncUpbitMarkets();

        List<CointradeTargetCoinEntity> activeCoins = targetCoinRepository.findByUseYn("Y");
        for (CointradeTargetCoinEntity entity : activeCoins) {
            entity.setIsActive(coinCodes.contains(entity.getCoinCode()));
            targetCoinRepository.save(entity);
        }

        log.info("대상 종목 설정 완료: 활성화 {}개, 전체 {}개", coinCodes.size(), activeCoins.size());
    }

    /**
     * Upbit 마켓 정보로 DB 동기화 (신규 추가, 상장폐지 처리)
     * is_active는 변경하지 않음
     */
    private void syncUpbitMarkets() {
        List<TradingParisDto> upbitMarkets = upbitService.getTradingPairs();

        if (upbitMarkets == null) {
            log.error("Upbit 거래 목록 조회 실패 - 동기화 건너뜀");
            return;
        }

        List<String> upbitMarketCodes = upbitMarkets.stream()
                .map(TradingParisDto::getMarket)
                .toList();

        // 1. Upbit에 없는 마켓은 use_yn = 'N'으로 비활성화
        List<CointradeTargetCoinEntity> existingCoins = targetCoinRepository.findAll();
        for (CointradeTargetCoinEntity entity : existingCoins) {
            if (!upbitMarketCodes.contains(entity.getCoinCode())) {
                entity.setUseYn("N");
                targetCoinRepository.save(entity);
            }
        }

        // 2. 신규 마켓 추가 + 기존 마켓 정보 갱신
        for (TradingParisDto market : upbitMarkets) {
            String marketCode = market.getMarket();

            CointradeTargetCoinEntity entity = targetCoinRepository.findByCoinCode(marketCode)
                    .orElseGet(() -> {
                        CointradeTargetCoinEntity newEntity = new CointradeTargetCoinEntity();
                        newEntity.setCoinCode(marketCode);
                        newEntity.setIsActive(false);
                        return newEntity;
                    });

            entity.setCoinName(market.getKoreanName());
            entity.setUseYn("Y");
            targetCoinRepository.save(entity);
        }

        log.info("Upbit 마켓 동기화 완료: {}개", upbitMarkets.size());
    }

    /**
     * 보유 종목 조회
     */
    @Transactional(readOnly = true)
    public List<CointradeHoldingDto> getAllHoldings() {
        return holdingRepository.findAll().stream()
                .map(entity -> CointradeHoldingDto.builder()
                        .coinCode(entity.getCoinCode())
                        .buyPrice(entity.getBuyPrice())
                        .quantity(entity.getQuantity())
                        .totalAmount(entity.getTotalAmount())
                        .peakPrice(entity.getPeakPrice())
                        .momentumScore(entity.getMomentumScore())
                        .mlConfidence(entity.getMlConfidence())
                        .entryReason(entity.getEntryReason())
                        .scannerSignalId(entity.getScannerSignalId())
                        .takeProfitPrice(entity.getTakeProfitPrice())
                        .stopLossPrice(entity.getStopLossPrice())
                        .maxHoldUntil(entity.getMaxHoldUntil())
                        .buyDate(entity.getBuyDate())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 거래 기록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<CointradeTradeHistoryDto> getTradeHistory(
            LocalDateTime startDate,
            LocalDateTime endDate,
            String coinCode,
            String tradeType,
            Pageable pageable) {

        Page<CointradeTradeHistoryEntity> entities;

        // 조건별 조회
        if (coinCode != null && !coinCode.isEmpty() && startDate != null && endDate != null) {
            // 종목 + 날짜 범위
            entities = tradeHistoryRepository.findByCoinCodeAndCreatedAtBetween(
                    coinCode, startDate, endDate, pageable);
        } else if (coinCode != null && !coinCode.isEmpty()) {
            // 종목별 조회
            entities = tradeHistoryRepository.findByCoinCodeOrderByCreatedAtDesc(
                    coinCode, pageable);
        } else if (startDate != null && endDate != null) {
            // 날짜 범위 조회
            entities = tradeHistoryRepository.findByCreatedAtBetween(
                    startDate, endDate, pageable);
        } else {
            // 전체 조회
            entities = tradeHistoryRepository.findByOrderByCreatedAtDesc(pageable);
        }

        return entities.map(entity -> CointradeTradeHistoryDto.builder()
                .id(entity.getId())
                .coinCode(entity.getCoinCode())
                .tradeType(entity.getTradeType())
                .price(entity.getPrice())
                .quantity(entity.getQuantity())
                .totalAmount(entity.getTotalAmount())
                .reason(entity.getReason())
                .profitLoss(entity.getProfitLoss())
                .profitLossRate(entity.getProfitLossRate())
                .momentumScore(entity.getMomentumScore())
                .mlConfidence(entity.getMlConfidence())
                .entryReason(entity.getEntryReason())
                .holdDurationSec(entity.getHoldDurationSec())
                .createdAt(entity.getCreatedAt())
                .build());
    }

    /**
     * 매수 스케줄러 ON/OFF (DB 설정 변경 후 파이썬 스케줄러 리로드)
     */
    @Transactional
    public void updateBuyScheduler(boolean enabled) {
        configRepository.findByParamName("SCANNER_ENABLED")
                .ifPresent(entity -> {
                    entity.setParamValue(String.valueOf(enabled));
                    configRepository.save(entity);
                    log.info("스캐너 설정 변경: {}", enabled);
                });
        reloadScheduler();
    }

    /**
     * 매도 스케줄러 ON/OFF
     */
    @Transactional
    public void updateSellScheduler(boolean enabled) {
        configRepository.findByParamName("SELL_ENABLED")
                .ifPresent(entity -> {
                    entity.setParamValue(String.valueOf(enabled));
                    configRepository.save(entity);
                    log.info("매도 설정 변경: {}", enabled);
                });
        reloadScheduler();
    }

    /**
     * 현재 상태 조회 (거래 기록 기반 집계)
     */
    @Transactional(readOnly = true)
    public CointradeStatusDto getStatus() {
        // 스케줄러 상태 조회 (v4.0: SCANNER_ENABLED / SELL_ENABLED)
        Boolean buySchedulerEnabled = configRepository.findByParamName("SCANNER_ENABLED")
                .map(entity -> Boolean.parseBoolean(entity.getParamValue()))
                .orElse(false);

        Boolean sellSchedulerEnabled = configRepository.findByParamName("SELL_ENABLED")
                .map(entity -> Boolean.parseBoolean(entity.getParamValue()))
                .orElse(false);

        String buyCheckHours = null; // v4.0에서 제거됨

        String sellCheckSeconds = configRepository.findByParamName("SELL_CHECK_SECONDS")
                .map(entity -> String.valueOf(entity.getParamValue()))
                .orElse(null);

        String priceMonitorSeconds = null; // v4.0에서 제거됨


        // 보유 종목 수
        Integer holdingCount = (int) holdingRepository.count();

        // 거래 기록 조회
        List<CointradeTradeHistoryEntity> allTrades = tradeHistoryRepository.findAll();

        // 매수/매도 건수
        long buyCount = allTrades.stream()
                .filter(trade -> "BUY".equals(trade.getTradeType()))
                .count();

        long sellCount = allTrades.stream()
                .filter(trade -> "SELL".equals(trade.getTradeType()))
                .count();

        // 총 손익 금액 집계
        BigDecimal totalProfitLoss = allTrades.stream()
                .map(CointradeTradeHistoryEntity::getProfitLoss)
                .filter(profit -> profit != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 총 수익률 계산 (총 손익 / 총 투자금액 * 100)
        BigDecimal totalInvestment = allTrades.stream()
                .filter(trade -> "BUY".equals(trade.getTradeType()))
                .map(CointradeTradeHistoryEntity::getTotalAmount)

                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalProfitLossRate = BigDecimal.ZERO;
        if (totalInvestment.compareTo(BigDecimal.ZERO) > 0) {
            totalProfitLossRate = totalProfitLoss
                    .divide(totalInvestment, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
        }

        return CointradeStatusDto.builder()
                .buySchedulerEnabled(buySchedulerEnabled)
                .sellSchedulerEnabled(sellSchedulerEnabled)
                .buyCheckHours(buyCheckHours)
                .buyNextRun(calculateNextRunTime())
                .sellCheckSeconds(sellCheckSeconds)
                .priceMonitorSeconds(priceMonitorSeconds)
                .holdingCount(holdingCount)
                .totalBuyCount((int) buyCount)
                .totalSellCount((int) sellCount)
                .totalProfitLoss(totalProfitLoss)
                .totalProfitLossRate(totalProfitLossRate)
                .build();
    }

    /**
     * 다음 실행 시간 계산
     */
    private CointradeNextRunDto calculateNextRunTime() {

        String url = buildUrl(CoinTraderProgramConfig.API_URI_BUY_NEXT_RUN);

        log.debug("다음 실행 시간 조회 API 호출 - URL: {}", url);

        return httpClientComponent
                .exchangeSync(
                        url,
                        HttpMethod.GET,
                        new ParameterizedTypeReference<CointradeNextRunDto>() {}
                )
                .getBody();
    }

    /**
     * 매수 프로세스 수동 실행
     */
    public Map<String, Object> startBuyProcess() {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_TRADE_BUY_START);
        log.info("매수 프로세스 수동 실행 요청 - URL: {}", url);
        return httpClientComponent.exchangeSync(url, HttpMethod.POST, new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    /**
     * 매도 프로세스 수동 실행
     */
    public Map<String, Object> startSellProcess() {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_TRADE_SELL_START);
        log.info("매도 프로세스 수동 실행 요청 - URL: {}", url);
        return httpClientComponent.exchangeSync(url, HttpMethod.POST, new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    /**
     * 매수/매도 프로세스 수동 중지
     */
    public Map<String, Object> stopProcess() {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_TRADE_STOP);
        log.info("매수/매도 프로세스 수동 중지 요청 - URL: {}", url);
        return httpClientComponent.exchangeSync(url, HttpMethod.POST, new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    /**
     * 모델 학습 수동 실행
     */
    public Map<String, Object> modelTrain(String coinCodes) {

        String url = buildUrl(CoinTraderProgramConfig.API_URI_MODEL_TRAIN);
        log.info("모델 학습 실행 요청 - URL: {}", url);

        List<String> coinCodeList = null;
        if(!StringUtil.isStringEmpty(coinCodes)) {
            coinCodeList = List.of(coinCodes.split(","));
        }

        String predictionDays = configRepository.findByParamName("PREDICTION_DAYS")
                .map(entity -> String.valueOf(entity.getParamValue()))
                .orElse(null);

        String ensembleMode = configRepository.findByParamName("ENSEMBLE_MODE")
                .map(entity -> String.valueOf(entity.getParamValue()))
                .orElse(null);

        Map<String, Object> param = new LinkedHashMap<>();
        param.put("coin_codes", coinCodeList);
        param.put("prediction_days", predictionDays == null ? "3" : predictionDays);
        param.put("ensemble_mode", ensembleMode == null ? "ensemble" : ensembleMode);

        return httpClientComponent.exchangeSync(url, HttpMethod.POST, null, param,new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    /**
     * 스케줄러 설정 즉시 리로드
     */
    public Map<String, Object> reloadScheduler() {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_SCHEDULER_RELOAD);
        log.info("스케줄러 설정 리로드 요청 - URL: {}", url);
        return httpClientComponent.exchangeSync(url, HttpMethod.POST, new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    /**
     * 업비트 KRW 잔액 조회
     */
    public Map<String, Object> getAccountBalance() {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_ACCOUNT_BALANCE);
        log.info("업비트 KRW 잔액 조회 요청 - URL: {}", url);
        return httpClientComponent.exchangeSync(url, HttpMethod.GET, new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    /**
     * 업비트 전체 계좌 조회 (KRW + 모든 보유 코인)
     * DB holdings 테이블과 대조해 수량 불일치를 UI에서 경고 표시하는 용도.
     */
    public Map<String, Object> getAccountBalances() {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_ACCOUNT_BALANCES);
        log.info("업비트 전체 계좌 조회 요청 - URL: {}", url);
        return httpClientComponent.exchangeSync(url, HttpMethod.GET, new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    /**
     * 보유 종목 매도
     */
    public CointradeSellResponseDto sellHoldings(CointradeSellRequestDto request) {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_HOLDINGS_SELL);
        log.info("보유 종목 매도 요청 - URL: {}, coinCodes: {}", url, request.getCoinCodes());

        Map<String, Object> param = new LinkedHashMap<>();
        if (request.getCoinCodes() != null && !request.getCoinCodes().isEmpty()) {
            param.put("coin_codes", request.getCoinCodes());
        }

        // 60초 타임아웃으로 호출 (매도는 시간이 오래 걸릴 수 있음)
        return httpClientComponent.exchangeSyncWithTimeout(
                url,
                HttpMethod.POST,
                null,
                param,
                new ParameterizedTypeReference<CointradeSellResponseDto>() {},
                300000  // 300초(5분), 종목 많으면 오래걸림
        ).getBody();
    }

    /**
     * 스캐너 시그널 목록 조회 (CoinTrader 프록시)
     */
    public List<Map<String, Object>> getScannerSignals() {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_SCANNER_SIGNALS);
        log.info("스캐너 시그널 목록 조회 요청 - URL: {}", url);
        return httpClientComponent.exchangeSync(url, HttpMethod.GET, new ParameterizedTypeReference<List<Map<String, Object>>>() {}).getBody();
    }

    /**
     * 스캐너 상태 조회 (CoinTrader 프록시)
     */
    public Map<String, Object> getScannerStatus() {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_SCANNER_STATUS);
        log.info("스캐너 상태 조회 요청 - URL: {}", url);
        return httpClientComponent.exchangeSync(url, HttpMethod.GET, new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    /**
     * 모의투자 보유 종목 조회 (CoinTrader 프록시)
     */
    public Object getPaperHoldings() {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_PAPER_HOLDINGS);
        log.info("모의투자 보유 종목 조회 요청 - URL: {}", url);
        return httpClientComponent.exchangeSync(url, HttpMethod.GET, new ParameterizedTypeReference<Object>() {}).getBody();
    }

    /**
     * 모의투자 거래 기록 조회 (CoinTrader 프록시)
     */
    public Object getPaperHistory(Integer limit) {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_PAPER_HISTORY);
        if (limit != null) {
            url += "?limit=" + limit;
        }
        log.info("모의투자 거래 기록 조회 요청 - URL: {}", url);
        return httpClientComponent.exchangeSync(url, HttpMethod.GET, new ParameterizedTypeReference<Object>() {}).getBody();
    }

    /**
     * 모의투자 데이터 초기화 (CoinTrader 프록시)
     */
    public Map<String, Object> resetPaperData() {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_PAPER_RESET);
        log.info("모의투자 데이터 초기화 요청 - URL: {}", url);
        return httpClientComponent.exchangeSync(url, HttpMethod.DELETE, new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    /**
     * 통합 모델 상태 조회 (CoinTrader 프록시)
     */
    public Map<String, Object> getModelStatus() {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_MODEL_STATUS);
        return httpClientComponent.exchangeSync(url, HttpMethod.GET, new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    /**
     * 재학습 진행률 상태 조회 (CoinTrader 프록시)
     */
    public Map<String, Object> getRetrainStatus() {
        String url = buildUrl(CoinTraderProgramConfig.API_URI_RETRAIN_STATUS);
        return httpClientComponent.exchangeSync(url, HttpMethod.GET, new ParameterizedTypeReference<Map<String, Object>>() {}).getBody();
    }

    private String buildUrl(String uri) {
        String baseUrl = isLocal
                ? CoinTraderProgramConfig.localHost
                : CoinTraderProgramConfig.prodHost;

        return baseUrl + "/" + uri;
    }
}
