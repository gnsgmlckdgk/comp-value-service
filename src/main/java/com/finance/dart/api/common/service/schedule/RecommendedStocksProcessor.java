package com.finance.dart.api.common.service.schedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.dart.api.abroad.dto.fmp.ratiosttm.RatiosTtmArrReqDto;
import com.finance.dart.api.abroad.dto.fmp.ratiosttm.RatiosTtmReqDto;
import com.finance.dart.api.abroad.dto.fmp.ratiosttm.RatiosTtmResDto;
import com.finance.dart.api.abroad.dto.fmp.stockscreener.StockScreenerReqDto;
import com.finance.dart.api.abroad.dto.fmp.stockscreener.StockScreenerResDto;
import com.finance.dart.api.abroad.service.fmp.RatiosTtmService;
import com.finance.dart.api.abroad.service.fmp.StockScreenerService;
import com.finance.dart.common.component.RedisComponent;
import com.finance.dart.common.component.RedisKeyGenerator;
import com.finance.dart.common.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 추천 종목 처리기
 * - Stock Screener로 후보군 조회
 * - RatiosTTM으로 저평가 종목 필터링
 * - Redis에 결과 저장 (TTL 24시간)
 */
@Slf4j
@AllArgsConstructor
@Service
public class RecommendedStocksProcessor {

    /** 테스트용 최대 처리 건수 (0이면 전체 처리) */
    public static final int MAX_PROCESS_COUNT_FOR_TEST = 0;

    /** 1분당 최대 API 호출 건수 (300건 제한, 200건으로 설정) */
    private static final int API_CALLS_PER_MINUTE = 200;

    /** API 호출 간격 (ms) - 1분 = 60,000ms */
    private static final long API_BATCH_INTERVAL_MS = 60_000L;

    /** Redis TTL (24시간) */
    private static final long REDIS_TTL_HOURS = 24;

    private final StockScreenerService stockScreenerService;
    private final RatiosTtmService ratiosTtmService;
    private final RedisComponent redisComponent;
    private final ObjectMapper objectMapper;

    /**
     * 추천 종목 처리 실행
     */
    public void process() {
        process(MAX_PROCESS_COUNT_FOR_TEST);
    }

    /**
     * 추천 종목 처리 실행 (최대 건수 지정)
     * @param maxCount 최대 처리 건수 (0이면 전체)
     */
    public void process(int maxCount) {
        log.info("[추천종목] 처리 시작");

        try {
            // 1. Stock Screener로 후보군 조회
            List<StockScreenerResDto> candidates = fetchCandidates();
            if (candidates == null || candidates.isEmpty()) {
                log.warn("[추천종목] 후보군이 없습니다.");
                return;
            }

            log.info("[추천종목] 후보군 조회 완료: {}건", candidates.size());

            // 최대 처리 건수 적용
            if (maxCount > 0 && candidates.size() > maxCount) {
                candidates = candidates.subList(0, maxCount);
                log.info("[추천종목] 최대 처리 건수 적용: {}건", maxCount);
            }

            // 2. 배치 단위로 RatiosTTM 조회 및 필터링
            Map<String, RecommendedStockData> undervaluedStocks = new LinkedHashMap<>();

            List<List<StockScreenerResDto>> batches = partitionList(candidates, API_CALLS_PER_MINUTE);
            log.info("[추천종목] 배치 수: {} (배치당 {}건)", batches.size(), API_CALLS_PER_MINUTE);

            for (int i = 0; i < batches.size(); i++) {
                List<StockScreenerResDto> batch = batches.get(i);
                log.info("[추천종목] 배치 {}/{} 처리 중 ({}건)", i + 1, batches.size(), batch.size());

                // RatiosTTM 병렬 조회
                Map<String, RecommendedStockData> batchResult = processBatch(batch);
                undervaluedStocks.putAll(batchResult);

                log.info("[추천종목] 배치 {}/{} 완료 - 저평가 종목: {}건", i + 1, batches.size(), batchResult.size());

                // 마지막 배치가 아니면 1분 대기 (API 호출 제한)
                if (i < batches.size() - 1) {
                    log.info("[추천종목] API 호출 제한으로 {}초 대기", API_BATCH_INTERVAL_MS / 1000);
                    Thread.sleep(API_BATCH_INTERVAL_MS);
                }
            }

            log.info("[추천종목] 필터링 완료 - 총 저평가 종목: {}건", undervaluedStocks.size());

            // 3. Redis에 저장
            saveToRedis(undervaluedStocks);

            log.info("[추천종목] 처리 완료");

        } catch (InterruptedException e) {
            log.error("[추천종목] 처리 중단됨", e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            log.error("[추천종목] 처리 중 오류 발생", e);
        }
    }

    /**
     * Stock Screener로 후보군 조회
     */
    private List<StockScreenerResDto> fetchCandidates() {
        StockScreenerReqDto reqDto = new StockScreenerReqDto();
        reqDto.setMarketCapMoreThan(2_000_000_000L);
        reqDto.setMarketCapLowerThan(500_000_000_000L);
        reqDto.setBetaLowerThan(1.5);
        reqDto.setVolumeMoreThan(100_000L);
        reqDto.setIsEtf(false);
        reqDto.setIsFund(false);
        reqDto.setIsActivelyTrading(true);
        reqDto.setExchange("NYSE,NASDAQ");
        reqDto.setLimit(10000);

        return stockScreenerService.findStockScreener(reqDto);
    }

    /**
     * 배치 단위로 RatiosTTM 조회 및 필터링
     */
    private Map<String, RecommendedStockData> processBatch(List<StockScreenerResDto> batch) {

        Map<String, RecommendedStockData> result = new LinkedHashMap<>();

        // 심볼 리스트 추출
        List<String> symbols = batch.stream()
                .map(StockScreenerResDto::getSymbol)
                .toList();

        // RatiosTTM 요청 DTO 생성
        List<RatiosTtmReqDto> reqDtoList = symbols.stream()
                .map(RatiosTtmReqDto::new)
                .toList();

        // 병렬 조회
        RatiosTtmArrReqDto arrReqDto = new RatiosTtmArrReqDto();
        arrReqDto.setSymbol(symbols);

        Map<String, List<RatiosTtmResDto>> ratiosMap = ratiosTtmService.findRatiosTTM(arrReqDto);

        // StockScreener 결과를 Map으로 변환 (빠른 조회용)
        Map<String, StockScreenerResDto> screenerMap = new HashMap<>();
        for (StockScreenerResDto dto : batch) {
            screenerMap.put(dto.getSymbol(), dto);
        }

        // 필터링 및 결과 생성
        for (Map.Entry<String, List<RatiosTtmResDto>> entry : ratiosMap.entrySet()) {
            String symbol = entry.getKey();
            List<RatiosTtmResDto> ratiosList = entry.getValue();

            if (ratiosList == null || ratiosList.isEmpty()) {
                continue;
            }

            RatiosTtmResDto ratios = ratiosList.get(0);

            // 저평가 필터링
            if (isUndervalued(ratios)) {
                StockScreenerResDto screenerData = screenerMap.get(symbol);
                RecommendedStockData stockData = new RecommendedStockData(screenerData, ratios);
                result.put(symbol, stockData);
            }
        }

        return result;
    }

    /**
     * 저평가 종목 판단
     */
    public boolean isUndervalued(RatiosTtmResDto ratios) {
        Double pe = ratios.getPeRatioTTM();
        Double pb = ratios.getPriceToBookRatioTTM();
        Double roe = ratios.getReturnOnEquityTTM();
        Double debtEquity = ratios.getDebtEquityRatioTTM();

        // 1. PER 기준 (핵심): 0 < PE < 15
        boolean validPE = pe != null && pe > 0 && pe < 15;

        // 2. PBR 기준: PB < 2 (null이면 통과)
        boolean validPB = pb == null || (pb > 0 && pb < 2);

        // 3. ROE 기준: ROE > 10% (null이면 통과)
        boolean validROE = roe == null || roe > 0.10;

        // 4. 부채비율: D/E < 1.0 (null이면 통과)
        boolean validDebt = debtEquity == null || debtEquity < 1.0;

        return validPE && validPB && validROE && validDebt;
    }

    /**
     * Redis에 저장
     */
    private void saveToRedis(Map<String, RecommendedStockData> undervaluedStocks) {
        try {
            // 전체 데이터를 하나의 키에 저장
            String allDataKey = RedisKeyGenerator.genRecommendedStocksAll();
            String allDataJson = objectMapper.writeValueAsString(undervaluedStocks);
            redisComponent.saveValueWithTtl(allDataKey, allDataJson, REDIS_TTL_HOURS, TimeUnit.HOURS);
            log.info("[추천종목] Redis 저장 완료 - Key: {}, 종목 수: {}", allDataKey, undervaluedStocks.size());

            // 개별 종목도 별도 저장 (선택적)
//            for (Map.Entry<String, RecommendedStockData> entry : undervaluedStocks.entrySet()) {
//                String symbol = entry.getKey();
//                RecommendedStockData data = entry.getValue();
//
//                String key = RedisKeyGenerator.genRecommendedStock(symbol);
//                String json = objectMapper.writeValueAsString(data);
//                redisComponent.saveValueWithTtl(key, json, REDIS_TTL_HOURS, TimeUnit.HOURS);
//            }

            log.info("[추천종목] 개별 종목 Redis 저장 완료: {}건", undervaluedStocks.size());

        } catch (JsonProcessingException e) {
            log.error("[추천종목] Redis 저장 중 JSON 변환 오류", e);
        }
    }

    /**
     * 리스트를 지정된 크기로 분할
     */
    private <T> List<List<T>> partitionList(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    /**
     * 추천 종목 데이터 (StockScreener + RatiosTTM 통합)
     */
    public record RecommendedStockData(
            // StockScreener 데이터
            String symbol,
            String companyName,
            Long marketCap,
            String sector,
            String industry,
            Double beta,
            Double price,
            Double lastAnnualDividend,
            Long volume,
            String exchange,
            String exchangeShortName,
            String country,

            // RatiosTTM 주요 지표
            Double peRatioTTM,
            Double priceToBookRatioTTM,
            Double returnOnEquityTTM,
            Double debtEquityRatioTTM,
            Double dividendYieldTTM,
            Double currentRatioTTM,
            Double netProfitMarginTTM,
            Double operatingProfitMarginTTM,
            Double returnOnAssetsTTM,
            Double priceToSalesRatioTTM,
            Double enterpriseValueMultipleTTM,

            // 부가 데이터
            String trscDt,
            String trscTm
    ) {
        public RecommendedStockData(StockScreenerResDto screener, RatiosTtmResDto ratios) {
            this(
                    screener.getSymbol(),
                    screener.getCompanyName(),
                    screener.getMarketCap(),
                    screener.getSector(),
                    screener.getIndustry(),
                    screener.getBeta(),
                    screener.getPrice(),
                    screener.getLastAnnualDividend(),
                    screener.getVolume(),
                    screener.getExchange(),
                    screener.getExchangeShortName(),
                    screener.getCountry(),
                    ratios.getPeRatioTTM(),
                    ratios.getPriceToBookRatioTTM(),
                    ratios.getReturnOnEquityTTM(),
                    ratios.getDebtEquityRatioTTM(),
                    ratios.getDividendYielTTM(),
                    ratios.getCurrentRatioTTM(),
                    ratios.getNetProfitMarginTTM(),
                    ratios.getOperatingProfitMarginTTM(),
                    ratios.getReturnOnAssetsTTM(),
                    ratios.getPriceToSalesRatioTTM(),
                    ratios.getEnterpriseValueMultipleTTM(),

                    DateUtil.getToday("yyyyMMdd"),
                    DateUtil.getToday("HHmmss")
            );
        }
    }
}
