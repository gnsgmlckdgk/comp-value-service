package com.finance.dart.cointrade.service;

import com.finance.dart.cointrade.dto.*;
import com.finance.dart.cointrade.dto.upbit.TradingParisDto;
import com.finance.dart.cointrade.entity.CointradeTargetCoinEntity;
import com.finance.dart.cointrade.entity.CointradeTradeHistoryEntity;
import com.finance.dart.cointrade.repository.CointradeConfigRepository;
import com.finance.dart.cointrade.repository.CointradeHoldingRepository;
import com.finance.dart.cointrade.repository.CointradeTargetCoinRepository;
import com.finance.dart.cointrade.repository.CointradeTradeHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
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
            configRepository.findByParamName(dto.getParamName())
                    .ifPresent(entity -> {
                        entity.setParamValue(dto.getParamValue());
                        configRepository.save(entity);
                    });
        }
        log.info("설정값 {}개 수정 완료", configs.size());
    }

    /**
     * 대상 종목 목록 조회 (DB 조회)
     */
    @Transactional(readOnly = true)
    public List<CointradeTargetCoinDto> getAllTargetCoins() {
        return targetCoinRepository.findAll().stream()
                .map(entity -> CointradeTargetCoinDto.builder()
                        .coinCode(entity.getCoinCode())
                        .coinName(entity.getCoinName())
                        .isActive(entity.getIsActive())
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * 대상 종목 설정 (선택한 종목들 is_active = true, 나머지 false)
     * Upbit 전체 마켓 조회 후 DB 동기화
     */
    @Transactional
    public void updateTargetCoins(List<String> coinCodes) {

        List<TradingParisDto> upbitMarkets = upbitService.getTradingPairs();

        if (upbitMarkets == null) {
            log.error("Upbit 거래 목록 조회 실패");
            return;
        }

        // 1. Upbit에 없는 마켓은 DB에서 삭제
        List<String> upbitMarketCodes = upbitMarkets.stream()
                .map(TradingParisDto::getMarket)
                .collect(Collectors.toList());

        List<CointradeTargetCoinEntity> existingCoins = targetCoinRepository.findAll();
        for (CointradeTargetCoinEntity entity : existingCoins) {
            if (!upbitMarketCodes.contains(entity.getCoinCode())) {
                targetCoinRepository.delete(entity);
            }
        }

        // 2. Upbit 마켓 정보로 DB 업데이트 (신규 추가 및 정보 갱신)
        for (TradingParisDto market : upbitMarkets) {
            String marketCode = market.getMarket();
            
            CointradeTargetCoinEntity entity = targetCoinRepository.findByCoinCode(marketCode)
                    .orElseGet(() -> {
                        CointradeTargetCoinEntity newEntity = new CointradeTargetCoinEntity();
                        newEntity.setCoinCode(marketCode);
                        return newEntity;
                    });

            entity.setCoinName(market.getKoreanName());
            entity.setIsActive(coinCodes.contains(marketCode));
            
            targetCoinRepository.save(entity);
        }

        log.info("대상 종목 설정 완료: 활성화 {}개, 전체 {}개", coinCodes.size(), upbitMarkets.size());
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
                        .predictedHigh(entity.getPredictedHigh())
                        .predictedLow(entity.getPredictedLow())
                        .buyDate(entity.getBuyDate())
                        .surgeProbability(entity.getSurgeProbability())
                        .surgeDay(entity.getSurgeDay())
                        .expireDate(entity.getExpireDate())
                        .buyScore(entity.getBuyScore())
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
                .buyScore(entity.getBuyScore())
                .surgeProbability(entity.getSurgeProbability())
                .createdAt(entity.getCreatedAt())
                .build());
    }

    /**
     * 매수 스케줄러 ON/OFF (DB 설정만 변경)
     */
    @Transactional
    public void updateBuyScheduler(boolean enabled) {
        configRepository.findByParamName("BUY_SCHEDULER_ENABLED")
                .ifPresent(entity -> {
                    entity.setParamValue(String.valueOf(enabled));
                    configRepository.save(entity);
                    log.info("매수 스케줄러 설정 변경: {}", enabled);
                });
    }

    /**
     * 매도 스케줄러 ON/OFF (DB 설정만 변경)
     */
    @Transactional
    public void updateSellScheduler(boolean enabled) {
        configRepository.findByParamName("SELL_SCHEDULER_ENABLED")
                .ifPresent(entity -> {
                    entity.setParamValue(String.valueOf(enabled));
                    configRepository.save(entity);
                    log.info("매도 스케줄러 설정 변경: {}", enabled);
                });
    }

    /**
     * 현재 상태 조회 (거래 기록 기반 집계)
     */
    @Transactional(readOnly = true)
    public CointradeStatusDto getStatus() {
        // 스케줄러 상태 조회
        Boolean buySchedulerEnabled = configRepository.findByParamName("BUY_SCHEDULER_ENABLED")
                .map(entity -> Boolean.parseBoolean(entity.getParamValue()))
                .orElse(false);

        Boolean sellSchedulerEnabled = configRepository.findByParamName("SELL_SCHEDULER_ENABLED")
                .map(entity -> Boolean.parseBoolean(entity.getParamValue()))
                .orElse(false);

        String buyCheckHours = configRepository.findByParamName("BUY_CHECK_HOURS")
                .map(entity -> String.valueOf(entity.getParamValue()))
                .orElse(null);

        String sellCheckSeconds = configRepository.findByParamName("SELL_CHECK_SECONDS")
                .map(entity -> String.valueOf(entity.getParamValue()))
                .orElse(null);

        String priceMonitorSeconds = configRepository.findByParamName("PRICE_MONITOR_SECONDS")
                .map(entity -> String.valueOf(entity.getParamValue()))
                .orElse(null);


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
                .sellCheckSeconds(sellCheckSeconds)
                .priceMonitorSeconds(priceMonitorSeconds)
                .holdingCount(holdingCount)
                .totalBuyCount((int) buyCount)
                .totalSellCount((int) sellCount)
                .totalProfitLoss(totalProfitLoss)
                .totalProfitLossRate(totalProfitLossRate)
                .build();
    }
}
