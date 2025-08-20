package com.finance.dart.api.abroad.service;

import com.finance.dart.api.abroad.dto.company.CompanyProfileDataResDto;
import com.finance.dart.api.abroad.dto.financial.statement.CommonFinancialStatementDto;
import com.finance.dart.api.abroad.dto.financial.statement.Shares;
import com.finance.dart.api.abroad.dto.financial.statement.USD;
import com.finance.dart.api.abroad.util.SecUtil;
import com.finance.dart.api.common.dto.CompanySharePriceCalculator;
import com.finance.dart.api.common.service.CompanySharePriceCalculatorService;
import com.finance.dart.common.util.CalUtil;
import com.finance.dart.common.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.List;

/**
 * 해외기업 주식가치 계산 서비스
 */
@Slf4j
@AllArgsConstructor
@Service
public class OverseasStockValueService {

    private final CompanySharePriceCalculatorService sharePriceCalculatorService;   // 가치 계산 서비스
    private final CompanyProfileSearchService profileSearchService;                 // 해외기업 정보조회 서비스
    private final AbroadFinancialStatementService financialStatementService;        // 해외기업 재무제표 조회 서비스

    // TODO: 개발중
    public void calPerValue(String symbol) {

        //@1. CIK 검색
        String cik = getCompanyCik(symbol);

        //@2. 계산
        try {
            calculator(cik);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        //@3. 결과 조립

    }


    /**
     * CIK 검색
     * @param symbol
     * @return
     */
    private String getCompanyCik(String symbol) {

        List<CompanyProfileDataResDto> companyList = profileSearchService.findProfileListBySymbol(symbol);
        if(companyList == null || companyList.size() != 1) return null;

        CompanyProfileDataResDto company = companyList.get(0);
        return StringUtil.defaultString(company.getCik());
    }

    /**
     * 계산
     * @param cik
     * @return
     */
    private Object calculator(String cik) throws InterruptedException {

        int delay = 200;    // 0.2

        CompanySharePriceCalculator sharePriceCalculator = new CompanySharePriceCalculator();
        sharePriceCalculator.setUnit("1");    // 1달러

        // 영업이익
        Thread.sleep(delay);
        USD[] incomeLossArr = getOperatingIncomeLoss(cik);
        for(int i = 0; i< incomeLossArr.length; i++) {
            if(log.isDebugEnabled()) log.debug("영업이익 최근 분기별({}) : {}", i, incomeLossArr[i]);
        }

        // 유동자산 합계
        Thread.sleep(delay);
        String assetsCurrent = getAssetsCurrent(cik);
        if(log.isDebugEnabled()) log.debug("유동자산 합계: {}", assetsCurrent);

        // 유동부채 합계
        Thread.sleep(delay);
        String liabilitiesCurrent = getLiabilitiesCurrent(cik);
        if(log.isDebugEnabled()) log.debug("유동부채 합계: {}", liabilitiesCurrent);

        // 유동비율 ( 유동비율(%) = (유동자산 ÷ 유동부채) × 100 )
        Thread.sleep(delay);
        String currentRatioPct = getCurrentRatioPct(assetsCurrent, liabilitiesCurrent);
        if(log.isDebugEnabled()) log.debug("유동비율: {}", currentRatioPct);
        if(currentRatioPct == null) return null;

        // 투자자산(비유동자산내)
        Thread.sleep(delay);
        String nocurrentInvestments = noncurrentInvestments(cik);
        if(log.isDebugEnabled()) log.debug("투자자산(비유동자산내) 합계: {}", nocurrentInvestments);

        // 고정부채(비유동부채)
        Thread.sleep(delay);
        String liabilitiesNoncurrent = getLiabilitiesNoncurrent(cik);
        if(log.isDebugEnabled()) log.debug("고정부채(비유동부채) 합계: {}", liabilitiesNoncurrent);

        // 발행주식수
        Thread.sleep(delay);
        String stock = getEntityCommonStockSharesOutstanding(cik);
        if(log.isDebugEnabled()) log.debug("발행주식수: {}", stock);

        return null;
    }

    /**
     * <pre>
     * 영업이익 조회
     * 최근 분기별 3개 조회
     * </pre>
     * @param cik
     * @return 0: 당기 / 1: 전기 / 2: 전전기
     */
    private USD[] getOperatingIncomeLoss(String cik) {

        CommonFinancialStatementDto incomeLoss = financialStatementService.findFS_OperatingIncomeLoss(cik);
        if(log.isDebugEnabled()) log.debug("영업이익 : {}", incomeLoss);

        List<USD> usdList = SecUtil.getUsdList(incomeLoss);
        USD lastUsd = SecUtil.getQuarterUsdByOffset(usdList, 0);        // 당기
        USD lastUsdB1 = SecUtil.getQuarterUsdByOffset(usdList, 1);      // 전기
        USD lastUsdB2 = SecUtil.getQuarterUsdByOffset(usdList, 2);      // 전전기

        USD[] usdArr = new USD[3];
        usdArr[0] = lastUsd;
        usdArr[1] = lastUsdB1;
        usdArr[2] = lastUsdB2;

        return usdArr;
    }

    /**
     * <pre>
     * 유동자산 합계 조회
     * 최근 데이터 조회
     * </pre>
     * @param cik
     * @return
     */
    private String getAssetsCurrent(String cik) {
        CommonFinancialStatementDto assetsCurrent = financialStatementService.findFS_AssetsCurrent(cik);
        List<USD> usdList = SecUtil.getUsdList(assetsCurrent);
        USD usd = SecUtil.getUsdByOffset(usdList, 0);   // 가장 최근 데이터

        return StringUtil.defaultString(usd.getVal());
    }

    /**
     * <pre>
     * 유동부채 합계 조회
     * 최근 데이터 조회
     * </pre>
     * @param cik
     * @return
     */
    private String getLiabilitiesCurrent(String cik) {
        CommonFinancialStatementDto liabilitiesCurrent = financialStatementService.findFS_LiabilitiesCurrent(cik);
        List<USD> usdList = SecUtil.getUsdList(liabilitiesCurrent);
        USD usd = SecUtil.getUsdByOffset(usdList, 0);

        return StringUtil.defaultString(usd.getVal());
    }

    /**
     * 비유동자산내 투자자산 조회
     * @param cik
     * @return
     */
    private String noncurrentInvestments(String cik) {

        String sum = "0";

        //@1. 장기매도 가능 증권
        CommonFinancialStatementDto response1 =
                financialStatementService.findFS_NI_AvailableForSaleSecuritiesNoncurrent(cik);
        List<USD> usdList1 = SecUtil.getUsdList(response1);
        USD usd1 = SecUtil.getUsdByOffset(usdList1, 0);
        if(log.isDebugEnabled()) log.debug("usd1 val = {}", usd1 != null ? usd1.getVal() : "0");

        if(usd1 != null) sum = CalUtil.add(sum, StringUtil.defaultString(usd1.getVal()));

        //@2. 지분법 투자
        CommonFinancialStatementDto response2 =
                financialStatementService.findFS_NI_LongTermInvestments(cik);
        List<USD> usdList2 = SecUtil.getUsdList(response2);
        USD usd2 = SecUtil.getUsdByOffset(usdList2, 0);
        if(log.isDebugEnabled()) log.debug("usd2 val = {}", usd2 != null ? usd2.getVal() : "0");

        if(usd2 != null) sum = CalUtil.add(sum, StringUtil.defaultString(usd2.getVal()));

        //@3. 기타 장기투자
        CommonFinancialStatementDto response3 =
                financialStatementService.findFS_NI_OtherInvestments(cik);
        List<USD> usdList3 = SecUtil.getUsdList(response3);
        USD usd3 = SecUtil.getUsdByOffset(usdList3, 0);
        if(log.isDebugEnabled()) log.debug("usd3 val = {}", usd3 != null ? usd3.getVal() : "0");

        if(usd3 != null) sum = CalUtil.add(sum, StringUtil.defaultString(usd3.getVal()));

        //@4. 투자 및 대여금
        CommonFinancialStatementDto response4 =
                financialStatementService.findFS_NI_InvestmentsAndAdvances(cik);
        List<USD> usdList4 = SecUtil.getUsdList(response4);
        USD usd4 = SecUtil.getUsdByOffset(usdList4, 0);
        if(log.isDebugEnabled()) log.debug("usd4 val = {}", usd4 != null ? usd4.getVal() : "0");

        if(usd4 != null) sum = CalUtil.add(sum, StringUtil.defaultString(usd4.getVal()));

        if(log.isDebugEnabled()) log.debug("sum = {}", sum);
        return sum;
    }

    /**
     * 유동비율 계산
     * @param assetsCurrent         유동자산 합계
     * @param liabilitiesCurrent    유동부채 합계
     * @return
     */
    private String getCurrentRatioPct(String assetsCurrent, String liabilitiesCurrent) {
        // 유동비율 ( 유동비율(%) = (유동자산 ÷ 유동부채) × 100 )
        if(StringUtil.isStringEmpty(liabilitiesCurrent) || liabilitiesCurrent.equals("0")) return null;
//        String ratio = CalUtil.divide(assetsCurrent, liabilitiesCurrent, 6, RoundingMode.HALF_UP);
//        String currentRatioPct = CalUtil.scale(CalUtil.multi(ratio, "100"), 2, RoundingMode.HALF_UP);

        String ratio = CalUtil.divide(assetsCurrent, liabilitiesCurrent, 2, RoundingMode.HALF_UP);  // 백분율은 제외

        return ratio;
    }

    /**
     * 고정부채(비유동부채) 합계 조회
     * @param cik
     * @return
     */
    private String getLiabilitiesNoncurrent(String cik) {
        CommonFinancialStatementDto liabilitiesNoncurrent = financialStatementService.findFS_LiabilitiesNoncurrent(cik);
        List<USD> usdList = SecUtil.getUsdList(liabilitiesNoncurrent);
        USD usd = SecUtil.getUsdByOffset(usdList, 0);

        return StringUtil.defaultString(usd.getVal());
    }

    /**
     * 발행주식수 조회
     * @param cik
     * @return
     */
    private String getEntityCommonStockSharesOutstanding(String cik) {
        CommonFinancialStatementDto stock = financialStatementService.findFS_EntityCommonStockSharesOutstanding(cik);
        List<Shares> sharesList = SecUtil.getSharesList(stock);
        Shares shares = SecUtil.getSharesByOffset(sharesList, 0);

        return StringUtil.defaultString(shares.getVal());
    }


}

