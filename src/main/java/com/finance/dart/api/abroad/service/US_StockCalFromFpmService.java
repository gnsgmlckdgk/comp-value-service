package com.finance.dart.api.abroad.service;

import com.finance.dart.api.abroad.consts.CurrencyConst;
import com.finance.dart.api.abroad.consts.FmpPeriod;
import com.finance.dart.api.abroad.dto.fmp.balancesheet.BalanceSheetReqDto;
import com.finance.dart.api.abroad.dto.fmp.balancesheet.BalanceSheetResDto;
import com.finance.dart.api.abroad.dto.fmp.chart.StockPriceVolumeReqDto;
import com.finance.dart.api.abroad.dto.fmp.chart.StockPriceVolumeResDto;
import com.finance.dart.api.abroad.dto.fmp.company.CompanyProfileDataResDto;
import com.finance.dart.api.abroad.dto.fmp.enterprisevalues.EnterpriseValuesReqDto;
import com.finance.dart.api.abroad.dto.fmp.enterprisevalues.EnterpriseValuesResDto;
import com.finance.dart.api.abroad.dto.fmp.financialgrowth.FinancialGrowthReqDto;
import com.finance.dart.api.abroad.dto.fmp.financialgrowth.FinancialGrowthResDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosReqDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosResDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosTTM_ReqDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosTTM_ResDto;
import com.finance.dart.api.abroad.dto.fmp.forexquote.ForexQuoteReqDto;
import com.finance.dart.api.abroad.dto.fmp.forexquote.ForexQuoteResDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatement.IncomeStatReqDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatement.IncomeStatResDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatgrowth.IncomeStatGrowthReqDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatgrowth.IncomeStatGrowthResDto;
import com.finance.dart.stockpredictor.dto.PredictionResponseDto;
import com.finance.dart.api.abroad.service.fmp.*;
import com.finance.dart.api.common.constants.RequestContextConst;
import com.finance.dart.api.common.context.RequestContext;
import com.finance.dart.api.common.dto.CompanySharePriceCalculator;
import com.finance.dart.api.common.dto.CompanySharePriceResult;
import com.finance.dart.api.common.dto.CompanySharePriceResultDetail;
import com.finance.dart.api.common.service.PerShareValueCalculationService;
import com.finance.dart.common.component.RedisComponent;
import com.finance.dart.common.component.RedisKeyGenerator;
import com.finance.dart.common.exception.BizException;
import com.finance.dart.common.util.CalUtil;
import com.finance.dart.common.util.DateUtil;
import com.finance.dart.common.util.StringUtil;
import com.finance.dart.stockpredictor.service.MlService;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 해외기업 주식가치 계산 서비스
 */
@Slf4j
@AllArgsConstructor
@Service
public class US_StockCalFromFpmService {

    private final int TRSC_DELAY = 10;    // 0.01s

    private final RequestContext requestContext;    // 요청 컨텍스트
    private final RedisComponent redisComponent;    // Redis 컴포넌트

    private final IncomeStatementService incomeStatementService;                // 영업이익 조회 서비스
    private final BalanceSheetStatementService balanceSheetStatementService;    // 재무상태표 조회 서비스
    private final EnterpriseValueService enterpriseValueService;                // 기업가치 조회 서비스
    private final FinancialRatiosService financialRatiosService;                // 재무비율지표 조회 서비스
    private final FinancialGrowthService financialGrowthService;                // 성장률 조회 서비스
    private final IncomeStatGrowthService incomeStatGrowthService;              // 영업이익 성장률 조회 서비스
    private final CompanyProfileSearchService profileSearchService;             // 해외기업 정보조회 서비스
    private final ForexQuoteService forexQuoteService;                          // 외환시세 조회 서비스
    private final StockPriceVolumeService stockPriceVolumeService;              // 주가 차트 조회 서비스

    private final PerShareValueCalculationService sharePriceCalculatorService;  // 가치 계산 서비스
    private final MlService mlService;                                          // 머신러닝 서비스


    /**
     * 주당 가치 계산
     * @param symbol
     * @return
     * @throws Exception
     */
    public CompanySharePriceResult calPerValue(String symbol) throws Exception {
        return calPerValueV5(symbol);
    }

    /**
     * 주당 가치 계산(다건)
     * @param symbols
     * @param detail
     * @return
     * @throws Exception
     */
    public List<CompanySharePriceResult> calPerValueList(String symbols, String detail) throws Exception {
        return calPerValueListV5(symbols, detail);
    }


    // ==============================================================

    /**
     * 주당 가치 계산(다건) V1
     * @param symbols
     * @param detail
     * @return
     * @throws Exception
     */
    public List<CompanySharePriceResult> calPerValueListV1(String symbols, String detail) throws Exception {

        List<CompanySharePriceResult> resultList = new LinkedList<>();

        if(symbols == null) return null;
        List<String> symbolList = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        for(String symbol : symbolList) {
            CompanySharePriceResult result = calPerValueV1(symbol);
            if("F".equals(StringUtil.defaultString(detail))) result.set상세정보(null);
            resultList.add(result);
            Thread.sleep(TRSC_DELAY);   // 너무 빠르게 연속호출하면 타겟에서 거부할 수 있음
        }

        return resultList;
    }

    /**
     * 주당 가치 계산(다건) V2
     * @param symbols
     * @param detail
     * @return
     * @throws Exception
     */
    public List<CompanySharePriceResult> calPerValueListV2(String symbols, String detail) throws Exception {

        List<CompanySharePriceResult> resultList = new LinkedList<>();

        if(symbols == null) return null;
        List<String> symbolList = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        for(String symbol : symbolList) {
            CompanySharePriceResult result = calPerValueV2(symbol);
            if("F".equals(StringUtil.defaultString(detail))) result.set상세정보(null);
            resultList.add(result);
            Thread.sleep(TRSC_DELAY);   // 너무 빠르게 연속호출하면 타겟에서 거부할 수 있음
        }

        return resultList;
    }

    /**
     * 주당 가치 계산(다건) V3
     * @param symbols
     * @param detail
     * @return
     * @throws Exception
     */
    public List<CompanySharePriceResult> calPerValueListV3(String symbols, String detail) throws Exception {

        final int MAX_CAL_SYMBOL_SIZE = 30; // 한번에 조회 가능 건수

        List<CompanySharePriceResult> resultList = new LinkedList<>();

        if(symbols == null) return null;
        List<String> symbolList = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if(symbolList.size() > MAX_CAL_SYMBOL_SIZE) {
            throw new BizException("동시 조회는 " + MAX_CAL_SYMBOL_SIZE + "건 까지만 가능합니다.");
        }

        for(String symbol : symbolList) {
            CompanySharePriceResult result = calPerValueV3(symbol);
            if("F".equals(StringUtil.defaultString(detail))) result.set상세정보(null);
            resultList.add(result);
            Thread.sleep(TRSC_DELAY);   // 너무 빠르게 연속호출하면 타겟에서 거부할 수 있음
        }

        return resultList;
    }

    /**
     * 주당 가치 계산(다건) V4 - 섹터별 차별화
     * @param symbols
     * @param detail
     * @return
     * @throws Exception
     */
    public List<CompanySharePriceResult> calPerValueListV4(String symbols, String detail) throws Exception {

        final int MAX_CAL_SYMBOL_SIZE = 30; // 한번에 조회 가능 건수

        List<CompanySharePriceResult> resultList = new LinkedList<>();

        if(symbols == null) return null;
        List<String> symbolList = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if(symbolList.size() > MAX_CAL_SYMBOL_SIZE) {
            throw new BizException("동시 조회는 " + MAX_CAL_SYMBOL_SIZE + "건 까지만 가능합니다.");
        }

        for(String symbol : symbolList) {
            CompanySharePriceResult result = calPerValueV4(symbol);
            if("F".equals(StringUtil.defaultString(detail))) result.set상세정보(null);
            resultList.add(result);
            Thread.sleep(TRSC_DELAY);   // 너무 빠르게 연속호출하면 타겟에서 거부할 수 있음
        }

        return resultList;
    }

    /**
     * 주당 가치 계산(다건) V5 - AI 예측 추가
     * @param symbols
     * @param detail
     * @return
     * @throws Exception
     */
    public List<CompanySharePriceResult> calPerValueListV5(String symbols, String detail) throws Exception {

        final int MAX_CAL_SYMBOL_SIZE = 30; // 한번에 조회 가능 건수

        List<CompanySharePriceResult> resultList = new LinkedList<>();

        if(symbols == null) return null;
        List<String> symbolList = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        if(symbolList.size() > MAX_CAL_SYMBOL_SIZE) {
            throw new BizException("동시 조회는 " + MAX_CAL_SYMBOL_SIZE + "건 까지만 가능합니다.");
        }

        for(String symbol : symbolList) {
            CompanySharePriceResult result = calPerValueV5(symbol);
            if("F".equals(StringUtil.defaultString(detail))) result.set상세정보(null);
            resultList.add(result);
            Thread.sleep(TRSC_DELAY);   // 너무 빠르게 연속호출하면 타겟에서 거부할 수 있음
        }

        return resultList;
    }


    /**
     * 주당 가치 계산 V1
     * @param symbol
     * @return
     */
    public CompanySharePriceResult calPerValueV1(String symbol)
            throws Exception {

        final String UNIT = "1";  // 1달러

        CompanySharePriceResult result = new CompanySharePriceResult(); // 결과
        result.set기업심볼(symbol);
        CompanySharePriceResultDetail resultDetail = new CompanySharePriceResultDetail(UNIT);

        //@1. 정보 조회 ---------------------------------
        CompanyProfileDataResDto companyProfile = getCompanyProfile(symbol, result);
        if(log.isDebugEnabled()) log.debug("기업 정보 = {}", companyProfile);
        if(companyProfile == null) errorProcess(result, "기업 정보 조회에 실패했습니다.");

        CompanySharePriceCalculator calParam = getCalParamData(symbol, result, resultDetail); // 계산 정보
        calParam.setUnit(UNIT);
        if(log.isDebugEnabled()) log.debug("계산 정보 = {}", calParam);
        if(calParam == null) errorProcess(result, "재무정보 조회에 실패했습니다.");


        //@2. 계산 ---------------------------------
        result.set주당가치(sharePriceCalculatorService.calPerValue(calParam));

        //@3. 결과 조립 ---------------------------------
        if(StringUtil.isStringEmpty(result.get결과메시지())) result.set결과메시지("정상 처리되었습니다.");
        result.set현재가격(StringUtil.defaultString(companyProfile.getPrice()));
        result.set확인시간(DateUtil.getToday("yyyy-MM-dd HH:mm:ss"));
        setRstDetailContextData(resultDetail);

        result.set상세정보(resultDetail);

        return result;
    }

    /**
     * 성장성 반영 주당 가치 계산
     * @param symbol
     * @return
     * @throws Exception
     */
    public CompanySharePriceResult calPerValueV2(String symbol)
            throws Exception {

        final String UNIT = "1";  // 1달러
        final String resultDataRedisKey = RedisKeyGenerator.genAbroadCompValueRstData(symbol, "v2");

        //@ Redis 저장값 확인(캐시 역할)
        String saveData = redisComponent.getValue(resultDataRedisKey);
        if(!StringUtil.isStringEmpty(saveData)) {
            return new Gson().fromJson(saveData, CompanySharePriceResult.class);
        }

        CompanySharePriceResult result = new CompanySharePriceResult(); // 결과
        result.set기업심볼(symbol);
        CompanySharePriceResultDetail resultDetail = new CompanySharePriceResultDetail(UNIT);

        //@1. 정보 조회 ---------------------------------
        CompanyProfileDataResDto companyProfile = getCompanyProfile(symbol, result);
        if(log.isDebugEnabled()) log.debug("기업 정보 = {}", companyProfile);
        if(companyProfile == null) errorProcess(result, "기업 정보 조회에 실패했습니다.");

        CompanySharePriceCalculator calParam = getCalParamDataV2(symbol, result, resultDetail); // 계산 정보
        calParam.setUnit(UNIT);
        if(log.isDebugEnabled()) log.debug("계산 정보 = {}", calParam);
        if(calParam == null) errorProcess(result, "재무정보 조회에 실패했습니다.");


        //@2. 계산 ---------------------------------
        result.set주당가치(sharePriceCalculatorService.calPerValueV2(calParam, resultDetail));

        //@3. 결과 조립 ---------------------------------
        if(StringUtil.isStringEmpty(result.get결과메시지())) result.set결과메시지("정상 처리되었습니다.");
        result.set현재가격(StringUtil.defaultString(companyProfile.getPrice()));
        result.set확인시간(DateUtil.getToday("yyyy-MM-dd HH:mm:ss"));
        setRstDetailContextData(resultDetail);

        result.set상세정보(resultDetail);

        //@ Redis에 결과값 저장(캐시 역할)
        redisComponent.saveValueWithTtl(resultDataRedisKey, new Gson().toJson(result), 6, TimeUnit.HOURS);

        return result;
    }

    /**
     * 고성장 적자 기업 매출액 기준 계산방식 추가
     * @param symbol
     * @return
     * @throws Exception
     */
    public CompanySharePriceResult calPerValueV3(String symbol)
            throws Exception {

        final String UNIT = "1";  // 1달러
        final String resultDataRedisKey = RedisKeyGenerator.genAbroadCompValueRstData(symbol, "v3");

        //@ Redis 저장값 확인(캐시 역할)
        String saveData = redisComponent.getValue(resultDataRedisKey);
        if(!StringUtil.isStringEmpty(saveData)) {
            return new Gson().fromJson(saveData, CompanySharePriceResult.class);
        }

        CompanySharePriceResult result = new CompanySharePriceResult(); // 결과
        result.set기업심볼(symbol);
        CompanySharePriceResultDetail resultDetail = new CompanySharePriceResultDetail(UNIT);

        //@1. 정보 조회 ---------------------------------
        CompanyProfileDataResDto companyProfile = getCompanyProfile(symbol, result);
        if(log.isDebugEnabled()) log.debug("기업 정보 = {}", companyProfile);
        if(companyProfile == null) errorProcess(result, "기업 정보 조회에 실패했습니다.");

        CompanySharePriceCalculator calParam = getCalParamDataV5(symbol, result, resultDetail); // 계산 정보
        calParam.setUnit(UNIT);
        if(log.isDebugEnabled()) log.debug("계산 정보 = {}", calParam);
        if(calParam == null) errorProcess(result, "재무정보 조회에 실패했습니다.");


        //@2. 계산 ---------------------------------
        String 계산된주당가치 = sharePriceCalculatorService.calPerValueV3(calParam, resultDetail);

        //@2-1. 3년내 최고가 조회 및 적정가 조정 ---------------------------------
        String 조정된주당가치;
        Double historicalHigh3Y = get3YearsHistoricalHighPrice(symbol);

        if (historicalHigh3Y != null) {
            BigDecimal 계산값 = new BigDecimal(계산된주당가치);
            BigDecimal 최고가 = BigDecimal.valueOf(historicalHigh3Y);

            if (계산값.compareTo(최고가) > 0) {
                // 계산된 적정가가 3년내 최고가보다 높으면 최고가의 80%로 조정
                조정된주당가치 = 최고가.multiply(new BigDecimal("0.8"))
                        .setScale(2, RoundingMode.HALF_UP)
                        .toPlainString();
                if(log.isDebugEnabled()) {
                    log.debug("[적정가 조정] {} - 계산값({}) > 3년내최고가({}) → 조정값({})",
                            symbol, 계산값, 최고가, 조정된주당가치);
                }
            } else {
                조정된주당가치 = 계산된주당가치;
            }
        } else {
            // 3년내 최고가 조회 실패시 원본값 사용 (신생기업 등)
            조정된주당가치 = 계산된주당가치;
            if(log.isDebugEnabled()) {
                log.debug("[적정가 조정] {} - 3년내 최고가 데이터 없음, 원본값 사용", symbol);
            }
        }

        //@3. 결과 조립 ---------------------------------
        result.set버전("V3");
        if(StringUtil.isStringEmpty(result.get결과메시지())) result.set결과메시지("정상 처리되었습니다.");
        result.set주당가치(조정된주당가치);              // 조정된 적정가
        result.set계산된주당가치(계산된주당가치);        // 원본 계산값
        result.set현재가격(StringUtil.defaultString(companyProfile.getPrice()));
        result.set확인시간(DateUtil.getToday("yyyy-MM-dd HH:mm:ss"));
        setRstDetailContextData(resultDetail);

        result.set상세정보(resultDetail);

        //@ Redis에 결과값 저장(캐시 역할)
        redisComponent.saveValueWithTtl(resultDataRedisKey, new Gson().toJson(result), 6, TimeUnit.HOURS);

        return result;
    }

    /**
     * 섹터별 차별화 주당가치 계산 V4
     * @param symbol
     * @return
     * @throws Exception
     */
    public CompanySharePriceResult calPerValueV4(String symbol)
            throws Exception {

        final String UNIT = "1";  // 1달러
        final String resultDataRedisKey = RedisKeyGenerator.genAbroadCompValueRstData(symbol, "v4");

        //@ Redis 저장값 확인(캐시 역할)
        String saveData = redisComponent.getValue(resultDataRedisKey);
        if(!StringUtil.isStringEmpty(saveData)) {
            return new Gson().fromJson(saveData, CompanySharePriceResult.class);
        }

        CompanySharePriceResult result = new CompanySharePriceResult(); // 결과
        result.set기업심볼(symbol);
        CompanySharePriceResultDetail resultDetail = new CompanySharePriceResultDetail(UNIT);

        //@1. 정보 조회 ---------------------------------
        CompanyProfileDataResDto companyProfile = getCompanyProfile(symbol, result);
        if(log.isDebugEnabled()) log.debug("기업 정보 = {}", companyProfile);
        if(companyProfile == null) errorProcess(result, "기업 정보 조회에 실패했습니다.");

        // 섹터 정보 추출
        String sector = companyProfile.getSector();
        if(log.isDebugEnabled()) log.debug("[V4] 섹터 정보: {}", sector);

        CompanySharePriceCalculator calParam = getCalParamDataV5(symbol, result, resultDetail); // 계산 정보 (V3와 동일)
        calParam.setUnit(UNIT);
        if(log.isDebugEnabled()) log.debug("계산 정보 = {}", calParam);
        if(calParam == null) errorProcess(result, "재무정보 조회에 실패했습니다.");


        //@2. 계산 (섹터 정보 전달) ---------------------------------
        String 계산된주당가치 = sharePriceCalculatorService.calPerValueV4(calParam, resultDetail, sector);

        //@2-1. 3년내 최고가 조회 및 적정가 조정 ---------------------------------
        String 조정된주당가치;
        Double historicalHigh3Y = get3YearsHistoricalHighPrice(symbol);

        if (historicalHigh3Y != null) {
            BigDecimal 계산값 = new BigDecimal(계산된주당가치);
            BigDecimal 최고가 = BigDecimal.valueOf(historicalHigh3Y);

            if (계산값.compareTo(최고가) > 0) {
                // 계산된 적정가가 3년내 최고가보다 높으면 최고가의 80%로 조정
                조정된주당가치 = 최고가.multiply(new BigDecimal("0.8"))
                                  .setScale(2, RoundingMode.HALF_UP)
                                  .toPlainString();
                if(log.isDebugEnabled()) {
                    log.debug("[적정가 조정] {} - 계산값({}) > 3년내최고가({}) → 조정값({})",
                             symbol, 계산값, 최고가, 조정된주당가치);
                }
            } else {
                조정된주당가치 = 계산된주당가치;
            }
        } else {
            // 3년내 최고가 조회 실패시 원본값 사용 (신생기업 등)
            조정된주당가치 = 계산된주당가치;
            if(log.isDebugEnabled()) {
                log.debug("[적정가 조정] {} - 3년내 최고가 데이터 없음, 원본값 사용", symbol);
            }
        }

        //@3. 결과 조립 ---------------------------------
        result.set버전("V4");
        result.set섹터(sector);  // 섹터 정보 추가
        if(StringUtil.isStringEmpty(result.get결과메시지())) result.set결과메시지("정상 처리되었습니다.");
        result.set주당가치(조정된주당가치);              // 조정된 적정가
        result.set계산된주당가치(계산된주당가치);        // 원본 계산값
        result.set현재가격(StringUtil.defaultString(companyProfile.getPrice()));
        result.set확인시간(DateUtil.getToday("yyyy-MM-dd HH:mm:ss"));
        setRstDetailContextData(resultDetail);

        result.set상세정보(resultDetail);

        //@ Redis에 결과값 저장(캐시 역할)
        redisComponent.saveValueWithTtl(resultDataRedisKey, new Gson().toJson(result), 6, TimeUnit.HOURS);

        return result;
    }

    /**
     * AI 예측 추가 (1주내 최고가 예측)
     * @param symbol
     * @return
     * @throws Exception
     */
    public CompanySharePriceResult calPerValueV5(String symbol)
            throws Exception {

        final String VERSION = "v5";    // EvaluationConst.java 도 수정 필요
        final String UNIT = "1";        // 1달러
        final String resultDataRedisKey = RedisKeyGenerator.genAbroadCompValueRstData(symbol, VERSION);

        //@ Redis 저장값 확인(캐시 역할)
        String saveData = redisComponent.getValue(resultDataRedisKey);
        if(!StringUtil.isStringEmpty(saveData)) {
            return new Gson().fromJson(saveData, CompanySharePriceResult.class);
        }

        CompanySharePriceResult result = new CompanySharePriceResult(); // 결과
        result.set기업심볼(symbol);
        CompanySharePriceResultDetail resultDetail = new CompanySharePriceResultDetail(UNIT);

        //@1. 정보 조회 ---------------------------------
        CompanyProfileDataResDto companyProfile = getCompanyProfile(symbol, result);
        if(log.isDebugEnabled()) log.debug("기업 정보 = {}", companyProfile);
        if(companyProfile == null) errorProcess(result, "기업 정보 조회에 실패했습니다.");

        // 섹터 정보 추출
        String sector = companyProfile.getSector();
        if(log.isDebugEnabled()) log.debug("섹터 정보: {}", sector);

        CompanySharePriceCalculator calParam = getCalParamDataV5(symbol, result, resultDetail); // 계산 정보
        calParam.setUnit(UNIT);
        if(log.isDebugEnabled()) log.debug("계산 정보 = {}", calParam);
        if(calParam == null) errorProcess(result, "재무정보 조회에 실패했습니다.");


        //@2. 계산 (섹터 정보 전달) ---------------------------------
        String 계산된주당가치 = sharePriceCalculatorService.calPerValueV5(calParam, resultDetail, sector);

        //@2-1. 3년내 최고가 조회 및 적정가 조정 ---------------------------------
        String 조정된주당가치;
        Double historicalHigh3Y = get3YearsHistoricalHighPrice(symbol);

        if (historicalHigh3Y != null) {
            BigDecimal 계산값 = new BigDecimal(계산된주당가치);
            BigDecimal 최고가 = BigDecimal.valueOf(historicalHigh3Y);

            if (계산값.compareTo(최고가) > 0) {
                // 계산된 적정가가 3년내 최고가보다 높으면 최고가의 80%로 조정
                조정된주당가치 = 최고가.multiply(new BigDecimal("0.8"))
                        .setScale(2, RoundingMode.HALF_UP)
                        .toPlainString();
                if(log.isDebugEnabled()) {
                    log.debug("[적정가 조정] {} - 계산값({}) > 3년내최고가({}) → 조정값({})",
                            symbol, 계산값, 최고가, 조정된주당가치);
                }
            } else {
                조정된주당가치 = 계산된주당가치;
            }
        } else {
            // 3년내 최고가 조회 실패시 원본값 사용 (신생기업 등)
            조정된주당가치 = 계산된주당가치;
            if(log.isDebugEnabled()) {
                log.debug("[적정가 조정] {} - 3년내 최고가 데이터 없음, 원본값 사용", symbol);
            }
        }

        //@3. AI 예측값 조회 ---------------------------------
        // 데이터 못가져오면 null
        PredictionResponseDto predictionResponseDto = predictWeeklyHigh(symbol);

        //@4. 결과 조립 ---------------------------------
        result.set버전(VERSION.toUpperCase());
        result.set섹터(sector);  // 섹터 정보 추가
        if(StringUtil.isStringEmpty(result.get결과메시지())) result.set결과메시지("정상 처리되었습니다.");
        result.set주당가치(조정된주당가치);              // 조정된 적정가
        result.set계산된주당가치(계산된주당가치);        // 원본 계산값
        result.set현재가격(StringUtil.defaultString(companyProfile.getPrice()));
        result.set확인시간(DateUtil.getToday("yyyy-MM-dd HH:mm:ss"));
        result.set예측데이터(predictionResponseDto);
        setRstDetailContextData(resultDetail);

        result.set상세정보(resultDetail);

        //@ Redis에 결과값 저장(캐시 역할)
        redisComponent.saveValueWithTtl(resultDataRedisKey, new Gson().toJson(result), 6, TimeUnit.HOURS);

        return result;
    }


    /**
     * 기업정보 조회
     * @param symbol
     * @param result
     * @return
     */
    private CompanyProfileDataResDto getCompanyProfile(String symbol, CompanySharePriceResult result) {

        List<CompanyProfileDataResDto> companyList = profileSearchService.findProfileListBySymbol(symbol);
        if(companyList == null || companyList.size() != 1) return null;

        CompanyProfileDataResDto company = companyList.get(0);

        result.set기업코드(company.getCik());
        result.set기업명(company.getCompanyName());
        result.set주식코드(company.getSymbol());

        return company;
    }

    /**
     * 계산정보 조회
     * @param symbol
     * @param result
     * @param resultDetail
     * @return
     */
    private CompanySharePriceCalculator getCalParamData(String symbol, CompanySharePriceResult result, CompanySharePriceResultDetail resultDetail)
            throws InterruptedException {

        CompanySharePriceCalculator calParam = new CompanySharePriceCalculator();

        //@ 필요데이터 조회
        Thread.sleep(TRSC_DELAY);
        IncomeStatReqDto incomeStatReqDto = new IncomeStatReqDto(symbol, 3, FmpPeriod.quarter);
        List<IncomeStatResDto> income = incomeStatementService.findIncomeStat(incomeStatReqDto);
        if(income == null || income.size() < 3) {
            result.set결과메시지("영업이익 조회에 실패했습니다.");
            return null;
        }

        Thread.sleep(TRSC_DELAY);
        BalanceSheetReqDto balanceSheetReqDto = new BalanceSheetReqDto(symbol, 1, FmpPeriod.quarter);
        List<BalanceSheetResDto> balance = balanceSheetStatementService.findBalanceSheet(balanceSheetReqDto);
        if(balance == null || balance.size() < 1) {
            result.set결과메시지("재무상태표 조회에 실패했습니다.");
            return null;
        }

        Thread.sleep(TRSC_DELAY);
        EnterpriseValuesReqDto enterpriseValuesReqDto = new EnterpriseValuesReqDto(symbol, 1, FmpPeriod.quarter);
        List<EnterpriseValuesResDto> enterpriseValues = enterpriseValueService.findEnterpriseValue(enterpriseValuesReqDto);
        if(enterpriseValues == null || enterpriseValues.size() < 1) {
            result.set결과메시지("기업가치 조회에 실패했습니다.");
            return null;
        }

        Thread.sleep(TRSC_DELAY);
        FinancialRatiosReqDto financialRatiosReqDto = new FinancialRatiosReqDto(symbol, 1, FmpPeriod.quarter);
        List<FinancialRatiosResDto> financialRatios = financialRatiosService.findFinancialRatios(financialRatiosReqDto);
        if(financialRatios == null || financialRatios.size() < 1) {
            result.set결과메시지("재무비율지표 조회에 실패했습니다.");
            return null;
        }

        //@ 영업이익
        setIncomeStat(calParam, income, resultDetail);

        //@ 유동자산 합계
        String assetsCurrent = StringUtil.defaultString(balance.get(0).getTotalCurrentAssets());
        calParam.setCurrentAssetsTotal(assetsCurrent);
        resultDetail.set유동자산합계(assetsCurrent);

        //@ 유동부채 합계
        String liabilitiesCurrent = StringUtil.defaultString(balance.get(0).getTotalCurrentLiabilities());
        calParam.setCurrentLiabilitiesTotal(liabilitiesCurrent);
        resultDetail.set유동부채합계(liabilitiesCurrent);

        //@ 유동비율
        String ratio = CalUtil.divide(assetsCurrent, liabilitiesCurrent, 2, RoundingMode.HALF_UP);  // 백분율은 제외
        calParam.setCurrentRatio(ratio);
        resultDetail.set유동비율(ratio);

        //@ 투자자산
        String longTermInvestments = StringUtil.defaultString(balance.get(0).getLongTermInvestments());
        calParam.setInvestmentAssets(longTermInvestments);
        resultDetail.set투자자산_비유동자산내(longTermInvestments);

        //@ 고정부채(비유동부채)
        String longTermDebt = StringUtil.defaultString(balance.get(0).getLongTermDebt());
        calParam.setFixedLiabilities(longTermDebt);
        resultDetail.set고정부채(longTermDebt);

        //@ 발행주식수
        String numberOfShares = StringUtil.defaultString(enterpriseValues.get(0).getNumberOfShares());
        calParam.setIssuedShares(numberOfShares);
        resultDetail.set발행주식수(numberOfShares);

        //@ PER
        String per = StringUtil.defaultString(financialRatios.get(0).getPriceToEarningsRatio());
        calParam.setPer(per);
        resultDetail.setPER(per);

        return calParam;
    }

    /**
     * 계산정보 조회(V2)
     * @param symbol
     * @param result
     * @param resultDetail
     * @return
     */
    private CompanySharePriceCalculator getCalParamDataV2(String symbol, CompanySharePriceResult result, CompanySharePriceResultDetail resultDetail)
            throws InterruptedException {

        CompanySharePriceCalculator calParam = new CompanySharePriceCalculator();

        //@ 필요데이터 조회
        Thread.sleep(TRSC_DELAY);
        IncomeStatReqDto incomeStatReqDto = new IncomeStatReqDto(symbol, 3, FmpPeriod.annual);  // V2 연간으로 변경
        List<IncomeStatResDto> income = incomeStatementService.findIncomeStat(incomeStatReqDto);
        if(income == null || income.size() < 3) {
            result.set결과메시지("영업이익 조회에 실패했습니다.");
            return null;
        } else {
            // 환율 반영
            for(IncomeStatResDto ic : income) {
                if(!ic.getReportedCurrency().equals(CurrencyConst.USA)) {
                    double rate = getForexQuotePriceUSD(ic.getReportedCurrency());
                    if(log.isDebugEnabled()) log.debug("[영업이익 환율 반영] {} -> USD / rate = {} / 변경전 = {}", ic.getReportedCurrency(), rate, ic);
                    if(rate != -1) ic.applyExchangeRate(rate);
                    if(log.isDebugEnabled()) log.debug("[영업이익 환율 반영] {} -> USD / rate = {} / 변경후 = {}", ic.getReportedCurrency(), rate, ic);
                }
            }
        }

        Thread.sleep(TRSC_DELAY);
        BalanceSheetReqDto balanceSheetReqDto = new BalanceSheetReqDto(symbol, 1, FmpPeriod.quarter);
        List<BalanceSheetResDto> balance = balanceSheetStatementService.findBalanceSheet(balanceSheetReqDto);
        if(balance == null || balance.size() < 1) {
            result.set결과메시지("재무상태표 조회에 실패했습니다.");
            return null;
        } else {
            // 환율 반영
            for(BalanceSheetResDto bs : balance) {
                if(!bs.getReportedCurrency().equals(CurrencyConst.USA)) {
                    double rate = getForexQuotePriceUSD(bs.getReportedCurrency());
                    if(log.isDebugEnabled()) log.debug("[재무상태표 환율 반영] {} -> USD / rate = {} / 변경전 = {}", bs.getReportedCurrency(), rate, bs);
                    if(rate != -1) bs.applyExchangeRate(rate);
                    if(log.isDebugEnabled()) log.debug("[재무상태표 환율 반영] {} -> USD / rate = {} / 변경후 = {}", bs.getReportedCurrency(), rate, bs);
                }
            }
        }

        Thread.sleep(TRSC_DELAY);
        EnterpriseValuesReqDto enterpriseValuesReqDto = new EnterpriseValuesReqDto(symbol, 1, FmpPeriod.quarter);
        List<EnterpriseValuesResDto> enterpriseValues = enterpriseValueService.findEnterpriseValue(enterpriseValuesReqDto);
        if(enterpriseValues == null || enterpriseValues.size() < 1) {
            result.set결과메시지("기업가치 조회에 실패했습니다.");
            return null;
        }

        Thread.sleep(TRSC_DELAY);
        FinancialRatiosTTM_ReqDto financialRatiosTTM_ReqDto = new FinancialRatiosTTM_ReqDto(symbol);
        List<FinancialRatiosTTM_ResDto> financialRatios = financialRatiosService.findFinancialRatiosTTM(financialRatiosTTM_ReqDto);
        if(financialRatios == null || financialRatios.size() < 1) {
            result.set결과메시지("재무비율지표(TTM) 조회에 실패했습니다.");
            return null;
        }

        FinancialGrowthReqDto financialGrowthReqDto = new FinancialGrowthReqDto(symbol, 1, FmpPeriod.fiscalYear);
        List<FinancialGrowthResDto> financialGrowth = financialGrowthService.financialStatementsGrowth(financialGrowthReqDto);
        if(financialGrowth == null || financialGrowth.size() < 1) {
            result.set결과메시지("성장률 조회에 실패했습니다.");
            return null;
        }

        IncomeStatGrowthReqDto incomeStatGrowthReqDto = new IncomeStatGrowthReqDto(symbol, 1, FmpPeriod.annual);
        List<IncomeStatGrowthResDto> incomeStatGrowth = incomeStatGrowthService.findIncomeStatGrowth(incomeStatGrowthReqDto);
        if(incomeStatGrowth == null || incomeStatGrowth.size() < 1) {
            result.set결과메시지("영업이익 성장률 조회에 실패했습니다.");
            return null;
        }

        // ----------------------------------------------------------------

        //@ 영업이익
        setIncomeStat(calParam, income, resultDetail);

        //@ 유동자산 합계
        String assetsCurrent = StringUtil.defaultString(balance.get(0).getTotalCurrentAssets());
        calParam.setCurrentAssetsTotal(assetsCurrent);
        resultDetail.set유동자산합계(assetsCurrent);

        //@ 유동부채 합계
        String liabilitiesCurrent = StringUtil.defaultString(balance.get(0).getTotalCurrentLiabilities());
        calParam.setCurrentLiabilitiesTotal(liabilitiesCurrent);
        resultDetail.set유동부채합계(liabilitiesCurrent);

        //@ 유동비율
        String ratio = CalUtil.divide(assetsCurrent, liabilitiesCurrent, 2, RoundingMode.HALF_UP);  // 백분율은 제외
        calParam.setCurrentRatio(ratio);
        resultDetail.set유동비율(ratio);

        //@ 투자자산
        String longTermInvestments = StringUtil.defaultString(balance.get(0).getLongTermInvestments());
        calParam.setInvestmentAssets(longTermInvestments);
        resultDetail.set투자자산_비유동자산내(longTermInvestments);

        //@ 무형자산
        String intangibleAssets = StringUtil.defaultString(balance.get(0).getIntangibleAssets());
        calParam.setIntangibleAssets(intangibleAssets);
        resultDetail.set무형자산(intangibleAssets);

        //@ 발행주식수
        String numberOfShares = StringUtil.defaultString(enterpriseValues.get(0).getNumberOfShares());
        calParam.setIssuedShares(numberOfShares);
        resultDetail.set발행주식수(numberOfShares);

        //@ PER
        String per = StringUtil.defaultString(financialRatios.get(0).getPriceToEarningsRatioTTM());
        calParam.setPer(per);
        resultDetail.setPER(per);

        //@ 성장률(연간)
        String epsGrowth = StringUtil.defaultString(financialGrowth.get(0).getEpsgrowth());
        calParam.setEpsgrowth(epsGrowth);
        resultDetail.setEPS성장률(epsGrowth);

        String incomeGrowth = StringUtil.defaultString(incomeStatGrowth.get(0).getGrowthOperatingIncome());
        calParam.setOperatingIncomeGrowth(incomeGrowth);
        resultDetail.set영업이익성장률(incomeGrowth);

        //@ 최근 3년 R&D
        setRnDStat(calParam, income, resultDetail);

        //@ 순부채
        String totalDebt = StringUtil.defaultString(balance.get(0).getTotalDebt());         // 총부채
        String cash = StringUtil.defaultString(balance.get(0).getCashAndCashEquivalents()); // 현금 및 현금성 자산
        if(StringUtil.isStringEmpty(cash) || "0".equals(cash))
            cash = StringUtil.defaultString(balance.get(0).getCashAndShortTermInvestments());

        calParam.setTotalDebt(totalDebt);
        calParam.setCashAndCashEquivalents(cash);

        resultDetail.set총부채(totalDebt);
        resultDetail.set현금성자산(cash);

        return calParam;
    }

    /**
     * 계산정보 조회(V3, V4, V5)
     * @param symbol
     * @param result
     * @param resultDetail
     * @return
     */
    private CompanySharePriceCalculator getCalParamDataV5(String symbol, CompanySharePriceResult result, CompanySharePriceResultDetail resultDetail)
            throws InterruptedException {

        if(log.isDebugEnabled()) log.debug("[계산 로그] [{}] {} 계산 시작", symbol, result.get기업명());

        CompanySharePriceCalculator calParam = new CompanySharePriceCalculator();

        //@ 필요데이터 조회
        Thread.sleep(TRSC_DELAY);
        IncomeStatReqDto incomeStatReqDto = new IncomeStatReqDto(symbol, 3, FmpPeriod.annual);  // V2 연간으로 변경
        List<IncomeStatResDto> income = incomeStatementService.findIncomeStat(incomeStatReqDto);
        if(income == null || income.size() < 3) {
            result.set결과메시지("영업이익 조회에 실패했습니다.");
            return null;
        } else {
            // 환율 반영
            for(IncomeStatResDto ic : income) {
                if(!ic.getReportedCurrency().equals(CurrencyConst.USA)) {
                    double rate = getForexQuotePriceUSD(ic.getReportedCurrency());
                    if(log.isDebugEnabled()) log.debug("[영업이익 환율 반영] {} -> USD / rate = {} / 변경전 = {}", ic.getReportedCurrency(), rate, ic);
                    if(rate != -1) ic.applyExchangeRate(rate);
                    if(log.isDebugEnabled()) log.debug("[영업이익 환율 반영] {} -> USD / rate = {} / 변경후 = {}", ic.getReportedCurrency(), rate, ic);
                }
            }
        }

        Thread.sleep(TRSC_DELAY);
        BalanceSheetReqDto balanceSheetReqDto = new BalanceSheetReqDto(symbol, 1, FmpPeriod.quarter);
        List<BalanceSheetResDto> balance = balanceSheetStatementService.findBalanceSheet(balanceSheetReqDto);
        if(balance == null || balance.size() < 1) {
            result.set결과메시지("재무상태표 조회에 실패했습니다.");
            return null;
        } else {
            // 환율 반영
            for(BalanceSheetResDto bs : balance) {
                if(!bs.getReportedCurrency().equals(CurrencyConst.USA)) {
                    double rate = getForexQuotePriceUSD(bs.getReportedCurrency());
                    if(log.isDebugEnabled()) log.debug("[재무상태표 환율 반영] {} -> USD / rate = {} / 변경전 = {}", bs.getReportedCurrency(), rate, bs);
                    if(rate != -1) bs.applyExchangeRate(rate);
                    if(log.isDebugEnabled()) log.debug("[재무상태표 환율 반영] {} -> USD / rate = {} / 변경후 = {}", bs.getReportedCurrency(), rate, bs);
                }
            }
        }

        Thread.sleep(TRSC_DELAY);
        EnterpriseValuesReqDto enterpriseValuesReqDto = new EnterpriseValuesReqDto(symbol, 1, FmpPeriod.quarter);
        List<EnterpriseValuesResDto> enterpriseValues = enterpriseValueService.findEnterpriseValue(enterpriseValuesReqDto);
        if(enterpriseValues == null || enterpriseValues.size() < 1) {
            result.set결과메시지("기업가치 조회에 실패했습니다.");
            return null;
        }

        Thread.sleep(TRSC_DELAY);
        FinancialRatiosTTM_ReqDto financialRatiosTTM_ReqDto = new FinancialRatiosTTM_ReqDto(symbol);
        List<FinancialRatiosTTM_ResDto> financialRatios = financialRatiosService.findFinancialRatiosTTM(financialRatiosTTM_ReqDto);
        if(financialRatios == null || financialRatios.size() < 1) {
            result.set결과메시지("재무비율지표(TTM) 조회에 실패했습니다.");
            return null;
        }

        FinancialGrowthReqDto financialGrowthReqDto = new FinancialGrowthReqDto(symbol, 1, FmpPeriod.fiscalYear);
        List<FinancialGrowthResDto> financialGrowth = financialGrowthService.financialStatementsGrowth(financialGrowthReqDto);
        if(financialGrowth == null || financialGrowth.size() < 1) {
            result.set결과메시지("성장률 조회에 실패했습니다.");
            return null;
        }

        IncomeStatGrowthReqDto incomeStatGrowthReqDto = new IncomeStatGrowthReqDto(symbol, 1, FmpPeriod.annual);
        List<IncomeStatGrowthResDto> incomeStatGrowth = incomeStatGrowthService.findIncomeStatGrowth(incomeStatGrowthReqDto);
        if(incomeStatGrowth == null || incomeStatGrowth.size() < 1) {
            result.set결과메시지("영업이익 성장률 조회에 실패했습니다.");
            return null;
        }

        // ----------------------------------------------------------------

        //@ 영업이익
        setIncomeStat(calParam, income, resultDetail);

        //@ 매출정보
        setPsr(calParam, income, incomeStatGrowth, financialRatios);

        //@ 유동자산 합계
        String assetsCurrent = StringUtil.defaultString(balance.get(0).getTotalCurrentAssets());
        calParam.setCurrentAssetsTotal(assetsCurrent);
        resultDetail.set유동자산합계(assetsCurrent);

        //@ 유동부채 합계
        String liabilitiesCurrent = StringUtil.defaultString(balance.get(0).getTotalCurrentLiabilities());
        calParam.setCurrentLiabilitiesTotal(liabilitiesCurrent);
        resultDetail.set유동부채합계(liabilitiesCurrent);

        //@ 유동비율
        String ratio;
        if("0".equals(liabilitiesCurrent) && "0".equals(assetsCurrent)) {
            // 둘 다 0인 경우: 보수적으로 1.0 설정
            ratio = "1.0";
        } else if("0".equals(liabilitiesCurrent)) {
            // 유동부채만 0인 경우: K값 계산에 영향 없음 (0 × K = 0)
            ratio = "2.0";
        } else if("0".equals(assetsCurrent)) {
            // 유동자산만 0인 경우: 매우 위험 → 전액 차감
            ratio = "0.0";
        } else {
            ratio = CalUtil.divide(assetsCurrent, liabilitiesCurrent, 2, RoundingMode.HALF_UP);
        }

        calParam.setCurrentRatio(ratio);
        resultDetail.set유동비율(ratio);

        //@ 투자자산
        String longTermInvestments = StringUtil.defaultString(balance.get(0).getLongTermInvestments());
        calParam.setInvestmentAssets(longTermInvestments);
        resultDetail.set투자자산_비유동자산내(longTermInvestments);

        //@ 무형자산 + 영업권 합산
        // 무형자산 + 영업권 합산
        String intangibleAssets = StringUtil.defaultString(balance.get(0).getGoodwillAndIntangibleAssets());
        calParam.setIntangibleAssets(intangibleAssets);
        resultDetail.set무형자산(intangibleAssets);

        //@ 발행주식수
        String numberOfShares = StringUtil.defaultString(enterpriseValues.get(0).getNumberOfShares());
        calParam.setIssuedShares(numberOfShares);
        resultDetail.set발행주식수(numberOfShares);

        //@ PER
        String per = StringUtil.defaultString(financialRatios.get(0).getPriceToEarningsRatioTTM());
        calParam.setPer(per);
        resultDetail.setPER(per);

        //@ 성장률(연간)
        String epsGrowth = StringUtil.defaultString(financialGrowth.get(0).getEpsgrowth());
        calParam.setEpsgrowth(epsGrowth);
        resultDetail.setEPS성장률(epsGrowth);

        String incomeGrowth = StringUtil.defaultString(incomeStatGrowth.get(0).getGrowthOperatingIncome());
        calParam.setOperatingIncomeGrowth(incomeGrowth);
        resultDetail.set영업이익성장률(incomeGrowth);

        //@ 최근 3년 R&D
        setRnDStat(calParam, income, resultDetail);

        //@ 순부채
        Long totalDebtVal = balance.get(0).getTotalDebt();
        Long capitalLease = balance.get(0).getCapitalLeaseObligations();
        String totalDebt = StringUtil.defaultString(
                (totalDebtVal != null ? totalDebtVal : 0L) +
                        (capitalLease != null ? capitalLease : 0L)
        );  // 총부채 + 금융리스부채

        String cash = StringUtil.defaultString(balance.get(0).getCashAndCashEquivalents()); // 현금 및 현금성 자산
        if(StringUtil.isStringEmpty(cash) || "0".equals(cash))
            cash = StringUtil.defaultString(balance.get(0).getCashAndShortTermInvestments());

        calParam.setTotalDebt(totalDebt);
        calParam.setCashAndCashEquivalents(cash);

        resultDetail.set총부채(totalDebt);
        resultDetail.set현금성자산(cash);

        return calParam;
    }


    /**
     * 영업이익 정보 세팅
     * @param calParam
     * @param incomeStat
     * @param resultDetail
     */
    private void setIncomeStat(CompanySharePriceCalculator calParam, List<IncomeStatResDto> incomeStat, CompanySharePriceResultDetail resultDetail) {

        String cIncome = StringUtil.defaultString(incomeStat.get(0).getOperatingIncome());
        String pIncome = StringUtil.defaultString(incomeStat.get(1).getOperatingIncome());
        String ppIncome = StringUtil.defaultString(incomeStat.get(2).getOperatingIncome());

        calParam.setOperatingProfitCurrent(cIncome);    // 당기
        calParam.setOperatingProfitPre(pIncome);        // 전기
        calParam.setOperatingProfitPrePre(ppIncome);    // 전전기

        resultDetail.set영업이익_당기(cIncome);
        resultDetail.set영업이익_전기(pIncome);
        resultDetail.set영업이익_전전기(ppIncome);
    }

    /**
     * 시가총액기준 매출 조회 (PSR)
     * @param calParam
     * @param income
     * @param incomeStatGrowth
     * @param financialRatios
     */
    private void setPsr(
            CompanySharePriceCalculator calParam,
            List<IncomeStatResDto> income,
            List<IncomeStatGrowthResDto> incomeStatGrowth,
            List<FinancialRatiosTTM_ResDto> financialRatios
    ) {

        //@ 매출액 (income 리스트에서 이미 조회됨)
        String revenueCurrent = StringUtil.defaultString(income.get(0).getRevenue());
        String revenuePre = StringUtil.defaultString(income.get(1).getRevenue());
        String revenuePrePre = StringUtil.defaultString(income.get(2).getRevenue());
        calParam.setRevenue(revenueCurrent);  // 또는 3년 평균 (지금은 당기)
        // 성장주 평가 목적으로 매출정보를 활용하기 때문에 당기만으로 계산

        //@ 매출 성장률 (incomeStatGrowth에서 조회)
        String revenueGrowth = StringUtil.defaultString(incomeStatGrowth.get(0).getGrowthRevenue());
        calParam.setRevenueGrowth(revenueGrowth);

        //@ PSR (financialRatios에서 조회)
        String psr = StringUtil.defaultString(financialRatios.get(0).getPriceToSalesRatioTTM());
        calParam.setPsr(psr);
    }


    /**
     * 연구개발비 세팅
     * @param calParam
     * @param incomeStat
     * @param resultDetail
     */
    private void setRnDStat(CompanySharePriceCalculator calParam, List<IncomeStatResDto> incomeStat, CompanySharePriceResultDetail resultDetail) {

        String cRnd = StringUtil.defaultString(incomeStat.get(0).getResearchAndDevelopmentExpenses());
        String pRnd = StringUtil.defaultString(incomeStat.get(1).getResearchAndDevelopmentExpenses());
        String ppRnd = StringUtil.defaultString(incomeStat.get(2).getResearchAndDevelopmentExpenses());

        calParam.setRndCurrent(cRnd);    // 당기
        calParam.setRndPre(pRnd);        // 전기
        calParam.setRndPrePre(ppRnd);    // 전전기

        resultDetail.set연구개발비_당기(cRnd);
        resultDetail.set연구개발비_전기(pRnd);
        resultDetail.set연구개발비_전전기(ppRnd);
    }

    /**
     * 오류 응답조립
     * @param result
     * @param message
     * @return
     */
    private CompanySharePriceResult errorProcess(CompanySharePriceResult result, String message) {

        if(result == null) result = new CompanySharePriceResult();

        if(StringUtil.isStringEmpty(result.get결과메시지())) {
            result.set결과메시지(message);

        } else {
            String rstMessage = result.get결과메시지();
            result.set결과메시지(message + "[" + rstMessage + "]");
        }

        result.set정상처리여부(false);
        return result;
    }

    /**
     * 결과 상세에 컨텍스트 저장데이터 세팅
     * @param resultDetail
     */
    private void setRstDetailContextData(CompanySharePriceResultDetail resultDetail) {
        resultDetail.set영업이익_합계(requestContext.getAttributeAsString(RequestContextConst.영업이익_합계));
        resultDetail.set영업이익_평균(requestContext.getAttributeAsString(RequestContextConst.영업이익_평균));

        resultDetail.set계산_사업가치(StringUtil.defaultString(requestContext.getAttributeAsString(RequestContextConst.계산_사업가치), "N/A"));
        resultDetail.set계산_재산가치(StringUtil.defaultString(requestContext.getAttributeAsString(RequestContextConst.계산_재산가치), "N/A"));
        resultDetail.set계산_부채(StringUtil.defaultString(requestContext.getAttributeAsString(RequestContextConst.계산_부채), "N/A"));
        resultDetail.set계산_기업가치(StringUtil.defaultString(requestContext.getAttributeAsString(RequestContextConst.계산_기업가치), "N/A"));

        if(StringUtil.isStringEmpty(resultDetail.getPER()))
            resultDetail.setPER(requestContext.getAttributeAsString(RequestContextConst.PER));
    }

    /**
     * 달러 환율정보 조회
     * @param currency
     * @return
     */
    private double getForexQuotePriceUSD(String currency) {

        String symbol = currency + "USD";

        ForexQuoteReqDto forexQuoteReqDto = new ForexQuoteReqDto(symbol);
        List<ForexQuoteResDto> resList = forexQuoteService.findForexQuote(forexQuoteReqDto);

        if(resList == null || resList.size() == 0) return -1;

        return resList.get(0).getPrice();
    }

    /**
     * 3년 내 최고가 조회
     * @param symbol 심볼
     * @return 3년 내 최고가 (조회 실패 또는 데이터 없으면 null)
     */
    private Double get3YearsHistoricalHighPrice(String symbol) {
        try {
            // 3년 전 날짜 계산
            LocalDate today = LocalDate.now();
            LocalDate threeYearsAgo = today.minusYears(3);

            StockPriceVolumeReqDto reqDto = new StockPriceVolumeReqDto(
                symbol,
                threeYearsAgo.format(DateTimeFormatter.ISO_LOCAL_DATE),
                today.format(DateTimeFormatter.ISO_LOCAL_DATE)
            );

            Thread.sleep(TRSC_DELAY);
            List<StockPriceVolumeResDto> priceHistory = stockPriceVolumeService.findStockPriceVolume(reqDto);

            // 데이터 없으면 null 반환 (신생기업의 경우 3년 데이터 없을 수 있음)
            if (priceHistory == null || priceHistory.isEmpty()) {
                if(log.isDebugEnabled()) log.debug("[3년내 최고가] {} - 데이터 없음 (신생기업 가능성)", symbol);
                return null;
            }

            // 최고가 추출
            Double highPrice = priceHistory.stream()
                .map(StockPriceVolumeResDto::getHigh)
                .filter(h -> h != null)
                .max(Double::compare)
                .orElse(null);

            if(log.isDebugEnabled()) log.debug("[3년내 최고가] {} - ${}", symbol, highPrice);
            return highPrice;

        } catch (Exception e) {
            log.warn("[3년내 최고가] {} - 조회 실패: {}", symbol, e.getMessage());
            return null;
        }
    }

    /**
     * 1주내 최고가 예측값 조회
     * @param symbol
     * @return
     */
    private PredictionResponseDto predictWeeklyHigh(String symbol) {

        try {
            PredictionResponseDto predictionResponseDto = mlService.getPrediction(symbol, false);
            return predictionResponseDto;
        } catch (Exception e) {
            if(log.isDebugEnabled()) log.debug("[predictWeeklyHigh] 예측 실패, message = {}", e.getMessage());
        }

        return null;
    }

}
