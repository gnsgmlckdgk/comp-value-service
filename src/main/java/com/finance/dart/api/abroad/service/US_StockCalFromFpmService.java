package com.finance.dart.api.abroad.service;

import com.finance.dart.api.abroad.consts.FmpPeriod;
import com.finance.dart.api.abroad.dto.fmp.balancesheet.BalanceSheetReqDto;
import com.finance.dart.api.abroad.dto.fmp.balancesheet.BalanceSheetResDto;
import com.finance.dart.api.abroad.dto.fmp.company.CompanyProfileDataResDto;
import com.finance.dart.api.abroad.dto.fmp.enterprisevalues.EnterpriseValuesReqDto;
import com.finance.dart.api.abroad.dto.fmp.enterprisevalues.EnterpriseValuesResDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosReqDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosResDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosTTM_ReqDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosTTM_ResDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatement.IncomeStatReqDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatement.IncomeStatResDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatgrowth.IncomeStatGrowthReqDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatgrowth.IncomeStatGrowthResDto;
import com.finance.dart.api.abroad.service.fmp.*;
import com.finance.dart.api.common.constants.RequestContextConst;
import com.finance.dart.api.common.context.RequestContext;
import com.finance.dart.api.common.dto.CompanySharePriceCalculator;
import com.finance.dart.api.common.dto.CompanySharePriceResult;
import com.finance.dart.api.common.dto.CompanySharePriceResultDetail;
import com.finance.dart.api.common.service.PerShareValueCalculationService;
import com.finance.dart.common.util.CalUtil;
import com.finance.dart.common.util.DateUtil;
import com.finance.dart.common.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 해외기업 주식가치 계산 서비스
 */
@Slf4j
@AllArgsConstructor
@Service
public class US_StockCalFromFpmService {

    private final int TRSC_DELAY = 100;    // 0.1s

    private final RequestContext requestContext;
    private final IncomeStatementService incomeStatementService;
    private final BalanceSheetStatementService balanceSheetStatementService;
    private final EnterpriseValueService enterpriseValueService;
    private final FinancialRatiosService financialRatiosService;
    private final IncomeStatGrowthService incomeStatGrowthService;
    private final CompanyProfileSearchService profileSearchService;              // 해외기업 정보조회 서비스

    private final PerShareValueCalculationService sharePriceCalculatorService;   // 가치 계산 서비스



    /**
     * 주당 가치 계산(다건)
     * @param symbols
     * @param detail
     * @return
     * @throws Exception
     */
    public List<CompanySharePriceResult> calPerValueList(String symbols, String detail) throws Exception {

        List<CompanySharePriceResult> resultList = new LinkedList<>();

        if(symbols == null) return null;
        List<String> symbolList = Arrays.stream(symbols.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();

        for(String symbol : symbolList) {
            CompanySharePriceResult result = calPerValue(symbol);
            if("F".equals(StringUtil.defaultString(detail))) result.set상세정보(null);
            resultList.add(result);
            Thread.sleep(TRSC_DELAY);   // 너무 빠르게 연속호출하면 타겟에서 거부할 수 있음
        }

        return resultList;
    }


    /**
     * 주당 가치 계산
     * @param symbol
     * @return
     */
    public CompanySharePriceResult calPerValue(String symbol)
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
        FinancialRatiosReqDto financialRatiosReqDto = new FinancialRatiosReqDto(symbol, 1, FmpPeriod.annual);   // TODO: 유료전환 후 querter 로 변경
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
        FinancialRatiosTTM_ReqDto financialRatiosTTM_ReqDto = new FinancialRatiosTTM_ReqDto(symbol);
        List<FinancialRatiosTTM_ResDto> financialRatios = financialRatiosService.findFinancialRatiosTTM(financialRatiosTTM_ReqDto);
        if(financialRatios == null || financialRatios.size() < 1) {
            result.set결과메시지("재무비율지표(TTM) 조회에 실패했습니다.");
            return null;
        }

        IncomeStatGrowthReqDto incomeStatGrowthReqDto = new IncomeStatGrowthReqDto(symbol, 1, FmpPeriod.annual);
        List<IncomeStatGrowthResDto> incomeStatGrowth = incomeStatGrowthService.findIncomeStatGrowth(incomeStatGrowthReqDto);
        if(incomeStatGrowth == null || incomeStatGrowth.size() < 1) {
            result.set결과메시지("재무제표 성장률 조회에 실패했습니다.");
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
        String growth = StringUtil.defaultString(incomeStatGrowth.get(0).getGrowthOperatingIncome());
        calParam.setOperatingIncomeGrowth(growth);
        resultDetail.set영업이익성장률(growth);

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
        resultDetail.set계산_사업가치(requestContext.getAttributeAsString(RequestContextConst.계산_사업가치));
        resultDetail.set계산_재산가치(requestContext.getAttributeAsString(RequestContextConst.계산_재산가치));
        resultDetail.set계산_부채(requestContext.getAttributeAsString(RequestContextConst.계산_부채));
        resultDetail.set계산_기업가치(requestContext.getAttributeAsString(RequestContextConst.계산_기업가치));
        resultDetail.setPER(requestContext.getAttributeAsString(RequestContextConst.PER));
    }
}
