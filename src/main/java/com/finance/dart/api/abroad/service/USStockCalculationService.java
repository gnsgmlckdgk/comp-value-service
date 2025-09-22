package com.finance.dart.api.abroad.service;

import com.finance.dart.api.abroad.dto.company.CompanyProfileDataResDto;
import com.finance.dart.api.abroad.dto.financial.statement.CommonFinancialStatementDto;
import com.finance.dart.api.abroad.dto.financial.statement.USD;
import com.finance.dart.api.abroad.util.SecUtil;
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
import java.util.List;

/**
 * 해외기업 주식가치 계산 서비스
 */
@Slf4j
@AllArgsConstructor
@Service
public class USStockCalculationService {

    private final RequestContext requestContext;
    private final PerShareValueCalculationService sharePriceCalculatorService;   // 가치 계산 서비스
    private final CompanyProfileSearchService profileSearchService;                 // 해외기업 정보조회 서비스
    private final AbroadFinancialStatementService financialStatementService;        // 해외기업 재무제표 조회 서비스

    private final String ERROR_DV_CD = "ERROR"; // 오류 구분코드(해당 값으로 응답시 오류로 판단)

    /**
     * 주당 가치 계산
     * @param symbol
     * @return
     */
    public CompanySharePriceResult calPerValue(String symbol) {

        CompanySharePriceResult result = null;

        //@1. CIK 검색
        CompanyProfileDataResDto companyProfile = getCompanyProfile(symbol);
        if(companyProfile == null) {
            result = new CompanySharePriceResult("기업정보가 조회되지 않았습니다.");
            return result;
        }

        //@2. 계산
        try {
            result = calculator(companyProfile);

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }

        //@3. 결과 조립
        if(StringUtil.isStringEmpty(result.get결과메시지())) result.set결과메시지("정상 처리되었습니다.");
        result.set기업코드(companyProfile.getCik());
        result.set기업명(companyProfile.getCompanyName());
        result.set주식코드(companyProfile.getSymbol());
        result.set현재가격(StringUtil.defaultString(companyProfile.getPrice()));
        result.set확인시간(DateUtil.getToday("yyyy-MM-dd HH:mm:ss"));

        return result;
    }


    /**
     * 기업 프로파일 조회
     * @param symbol
     * @return
     */
    private CompanyProfileDataResDto getCompanyProfile(String symbol) {

        List<CompanyProfileDataResDto> companyList = profileSearchService.findProfileListBySymbol(symbol);
        if(companyList == null || companyList.size() != 1) return null;

        CompanyProfileDataResDto company = companyList.get(0);
        return company;
    }

    /**
     * 계산
     * @param companyProfile
     * @return
     */
    private CompanySharePriceResult calculator(CompanyProfileDataResDto companyProfile) throws InterruptedException {

        // -------------------------------------------------------------
        int DELAY = 200;    // 0.2
        final String UNIT = "1";  // 1달러

        CompanySharePriceResult result = new CompanySharePriceResult();
        CompanySharePriceResultDetail resultDetail = new CompanySharePriceResultDetail(UNIT);

        String cik = companyProfile.getCik();

        CompanySharePriceCalculator sharePriceCalculator = new CompanySharePriceCalculator();
        sharePriceCalculator.setUnit(UNIT);    // 1달러
        // -------------------------------------------------------------

        //@ 영업이익
        Thread.sleep(DELAY);
        USD[] incomeLossArr = getOperatingIncomeLoss(cik, sharePriceCalculator, resultDetail);
        for(int i = 0; i< incomeLossArr.length; i++) {  // 로그출력용 반복
            if(log.isDebugEnabled()) log.debug("영업이익 최근 분기별({}) : {}", i, incomeLossArr[i]);
        }

        //@ 유동자산 합계
        Thread.sleep(DELAY);
        String assetsCurrent = getAssetsCurrent(cik, sharePriceCalculator, resultDetail);
        if(log.isDebugEnabled()) log.debug("유동자산 합계: {}", assetsCurrent);

        //@ 유동부채 합계
        Thread.sleep(DELAY);
        String liabilitiesCurrent = getLiabilitiesCurrent(cik, sharePriceCalculator, resultDetail);
        if(log.isDebugEnabled()) log.debug("유동부채 합계: {}", liabilitiesCurrent);

        //@ 유동비율 ( 유동비율(%) = (유동자산 ÷ 유동부채) × 100 )
        Thread.sleep(DELAY);
        String currentRatioPct = getCurrentRatioPct(assetsCurrent, liabilitiesCurrent, sharePriceCalculator, resultDetail);
        if(log.isDebugEnabled()) log.debug("유동비율: {}", currentRatioPct);
        if(currentRatioPct == null) return null;

        //@ 투자자산(비유동자산내)
        Thread.sleep(DELAY);
        String nocurrentInvestments = noncurrentInvestments(cik, sharePriceCalculator, resultDetail);
        if(log.isDebugEnabled()) log.debug("투자자산(비유동자산내) 합계: {}", nocurrentInvestments);

        //@ 고정부채(비유동부채)
        Thread.sleep(DELAY);
        String liabilitiesNoncurrent = getLiabilitiesNoncurrent(cik, sharePriceCalculator, resultDetail);
        if(log.isDebugEnabled()) log.debug("고정부채(비유동부채) 합계: {}", liabilitiesNoncurrent);
        if(liabilitiesNoncurrent.equals(this.ERROR_DV_CD)) return errorProcess(result, "고정부채(비유동부채) 조회 중 오류가 발생했습니다.");

        //@ 발행주식수
        Thread.sleep(DELAY);
        String stock = getEntityCommonStockSharesOutstanding(cik, sharePriceCalculator, resultDetail);
        if(log.isDebugEnabled()) log.debug("발행주식수: {}", stock);
        if(stock.equals("")) return errorProcess(result, "발행주식수 정보가 조회되지 않았습니다.");

        //@ 최종 계산
        if(log.isDebugEnabled()) log.debug("계산정보 DTO = {}", sharePriceCalculator);

        result.set주당가치(sharePriceCalculatorService.calPerValue(sharePriceCalculator));
        setRstDetailContextData(resultDetail);  // Context 저장데이터 세팅
        result.set상세정보(resultDetail);

        return result;
    }

    /**
     * <pre>
     * 영업이익 조회
     * 최근 분기별 3개 조회
     * </pre>
     * @param cik
     * @param spc
     * @param rstDetail
     * @return 0: 당기 / 1: 전기 / 2: 전전기
     */
    private USD[] getOperatingIncomeLoss(String cik, CompanySharePriceCalculator spc, CompanySharePriceResultDetail rstDetail) {

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

        //@ DTO 세팅
        // 계산용
        spc.setOperatingProfitCurrent(StringUtil.defaultString(lastUsd.getVal()));
        spc.setOperatingProfitPre(StringUtil.defaultString(lastUsdB1.getVal()));
        spc.setOperatingProfitPrePre(StringUtil.defaultString(lastUsdB2.getVal()));

        // 결과 상세
        rstDetail.set영업이익_전전기(StringUtil.defaultString(lastUsdB2.getVal()));
        rstDetail.set영업이익_전기(StringUtil.defaultString(lastUsdB1.getVal()));
        rstDetail.set영업이익_당기(StringUtil.defaultString(lastUsd.getVal()));

        return usdArr;
    }

    /**
     * <pre>
     * 유동자산 합계 조회
     * 최근 데이터 조회
     * </pre>
     * @param cik
     * @param spc
     * @param rstDetail
     * @return
     */
    private String getAssetsCurrent(String cik, CompanySharePriceCalculator spc, CompanySharePriceResultDetail rstDetail) {
        CommonFinancialStatementDto assetsCurrent = financialStatementService.findFS_AssetsCurrent(cik);
        List<USD> usdList = SecUtil.getUsdList(assetsCurrent);
        USD usd = SecUtil.getUsdByOffset(usdList, 0);   // 가장 최근 데이터

        String result = StringUtil.defaultString(usd.getVal());
        spc.setCurrentAssetsTotal(result);
        rstDetail.set유동자산합계(result);

        return result;
    }

    /**
     * <pre>
     * 유동부채 합계 조회
     * 최근 데이터 조회
     * </pre>
     * @param cik
     * @param spc
     * @param rstDetail
     * @return
     */
    private String getLiabilitiesCurrent(String cik, CompanySharePriceCalculator spc, CompanySharePriceResultDetail rstDetail) {
        CommonFinancialStatementDto liabilitiesCurrent = financialStatementService.findFS_LiabilitiesCurrent(cik);
        List<USD> usdList = SecUtil.getUsdList(liabilitiesCurrent);
        USD usd = SecUtil.getUsdByOffset(usdList, 0);

        String result = StringUtil.defaultString(usd.getVal());
        spc.setCurrentLiabilitiesTotal(result);
        rstDetail.set유동부채합계(result);

        return result;
    }

    /**
     * 비유동자산내 투자자산 조회
     * @param cik
     * @param spc
     * @param rstDetail
     * @return
     */
    private String noncurrentInvestments(String cik, CompanySharePriceCalculator spc, CompanySharePriceResultDetail rstDetail) {

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

        spc.setInvestmentAssets(sum);
        rstDetail.set투자자산_비유동자산내(sum);

        if(log.isDebugEnabled()) log.debug("sum = {}", sum);
        return sum;
    }

    /**
     * 유동비율 계산
     * @param assetsCurrent         유동자산 합계
     * @param liabilitiesCurrent    유동부채 합계
     * @param spc
     * @param rstDetail
     * @return
     */
    private String getCurrentRatioPct(String assetsCurrent, String liabilitiesCurrent, CompanySharePriceCalculator spc, CompanySharePriceResultDetail rstDetail) {
        // 유동비율 ( 유동비율(%) = (유동자산 ÷ 유동부채) × 100 )
        if(StringUtil.isStringEmpty(liabilitiesCurrent) || liabilitiesCurrent.equals("0")) return null;
//        String ratio = CalUtil.divide(assetsCurrent, liabilitiesCurrent, 6, RoundingMode.HALF_UP);
//        String currentRatioPct = CalUtil.scale(CalUtil.multi(ratio, "100"), 2, RoundingMode.HALF_UP);

        String ratio = CalUtil.divide(assetsCurrent, liabilitiesCurrent, 2, RoundingMode.HALF_UP);  // 백분율은 제외

        spc.setCurrentRatio(ratio);
        rstDetail.set유동비율(ratio);

        return ratio;
    }

    /**
     * 고정부채(비유동부채) 합계 조회
     * @param cik
     * @param spc
     * @param rstDetail
     * @return
     */
    private String getLiabilitiesNoncurrent(String cik, CompanySharePriceCalculator spc, CompanySharePriceResultDetail rstDetail) {
        CommonFinancialStatementDto liabilitiesNoncurrent = financialStatementService.findFS_LiabilitiesNoncurrent(cik);
        if(liabilitiesNoncurrent == null) return this.ERROR_DV_CD;

        List<USD> usdList = SecUtil.getUsdList(liabilitiesNoncurrent);
        USD usd = SecUtil.getUsdByOffset(usdList, 0);

        if(usd == null) return this.ERROR_DV_CD;

        String result = StringUtil.defaultString(usd.getVal());
        spc.setFixedLiabilities(result);
        rstDetail.set고정부채(result);

        return result;
    }

    /**
     * 발행주식수 조회
     * @param cik
     * @param spc
     * @param rstDetail
     * @return
     */
    private String getEntityCommonStockSharesOutstanding(String cik, CompanySharePriceCalculator spc, CompanySharePriceResultDetail rstDetail) {

        /** 2025.09.11 : 발행주식수 정보가 특정 태그에 확정적으로 고정이 안되어있어서
         *  회사 전체 facts에서 최신 outstanding을 우선 탐색하고,
         *  실패 시 기존 단일 태그(EntityCommonStockSharesOutstanding) 방식으로 폴백한다.
         **/

        String result = financialStatementService.getStockSharesOutstanding(cik);

        spc.setIssuedShares(result);    // 계산 정보 객체에 발행주식수 세팅
        rstDetail.set발행주식수(result);

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
    }

    /**
     * 오류 처리
     * @param result
     * @param message
     * @return
     */
    private CompanySharePriceResult errorProcess(CompanySharePriceResult result, String message) {
        result.set결과메시지(message);
        result.set정상처리여부(false);
        return result;
    }

}

