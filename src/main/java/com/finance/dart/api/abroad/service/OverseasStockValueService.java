package com.finance.dart.api.abroad.service;

import com.finance.dart.api.abroad.dto.company.CompanyProfileDataResDto;
import com.finance.dart.api.abroad.dto.financial.statement.CommonFinancialStatementDto;
import com.finance.dart.api.abroad.dto.financial.statement.USD;
import com.finance.dart.api.abroad.util.SecUtil;
import com.finance.dart.api.common.dto.CompanySharePriceCalculator;
import com.finance.dart.api.common.service.CompanySharePriceCalculatorService;
import com.finance.dart.common.util.StringUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

        //@2.
        calculator(cik);

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
    private Object calculator(String cik) {

        CompanySharePriceCalculator sharePriceCalculator = new CompanySharePriceCalculator();
        sharePriceCalculator.setUnit("1");    // 1달러

        // 영업이익(전전기, 전기, 당기)
        USD[] incomeLossArr = getOperatingIncomeLoss(cik);
        if(log.isDebugEnabled()) log.debug("영업이익 최근 분기별(3) : {}", incomeLossArr);

        // 유동자산 합계
        USD AssetsCurrent = getAssetsCurrent(cik);

        // 유동부채 합계

        // 유동비율

        // 투자자산(비유동자산내)

        // 고정부채(비유동부채)

        // 발행주식수

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
     * 유동자산 합계는 시점값만 존재(그날 기준 얼마인지만 존재)
     * </pre>
     * @param cik
     * @return
     */
    private USD getAssetsCurrent(String cik) {
        CommonFinancialStatementDto assetsCurrent = financialStatementService.findFS_AssetsCurrent(cik);
        List<USD> usdList = SecUtil.getUsdList(assetsCurrent);
        USD usd = SecUtil.getUsdByOffset(usdList, 0);   // 가장 최근 데이터

        return usd;
    }

}
