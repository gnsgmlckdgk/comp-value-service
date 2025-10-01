package com.finance.dart.api.common.service;

import com.finance.dart.api.abroad.service.sec.SecFinStatementService;
import com.finance.dart.api.common.constants.RequestContextConst;
import com.finance.dart.api.common.context.RequestContext;
import com.finance.dart.api.common.dto.CompanySharePriceCalculator;
import com.finance.dart.common.util.CalUtil;
import com.finance.dart.common.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;

/**
 * 한 주당 가치 계산 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PerShareValueCalculationService {

    private final RequestContext requestContext;

    private final SecFinStatementService financialStatementService;     // 해외기업 재무제표 조회 서비스


    /**
     * 한 주당 가치를 계산한다.
     *
     * @param req 계산에 필요한 데이터가 담긴 StockValueManualReqDTO
     * @return 한 주 가격
     */
    public String calPerValue(CompanySharePriceCalculator req) {

        if(log.isDebugEnabled()) log.debug("CompanySharePriceCalculator = {}", req);

        // 1. 영업이익 평균 계산
        final String operatingProfitAvg = calOperatingProfitAvg(
                req.getOperatingProfitPrePre(),
                req.getOperatingProfitPre(),
                req.getOperatingProfitCurrent()
        );
        if(log.isDebugEnabled()) log.debug("영업이익 평균 = {}", operatingProfitAvg);

        // 요청 객체에서 필요한 값들을 final 변수로 할당
        final String assetsTotal = req.getCurrentAssetsTotal();             // 유동자산합계
        final String liabilitiesTotal = req.getCurrentLiabilitiesTotal();   // 유동부채합계
        final String currentRatio = req.getCurrentRatio();                  // 유동비율
        final String investmentAssets = req.getInvestmentAssets();          // 투자자산 (비유동자산 내)
        final String fixedLiabilities = req.getFixedLiabilities();          // 고정부채 (비유동부채)
        final String issuedShares = req.getIssuedShares();                  // 발행주식수

        // 2. 각 단계별 계산

        // 사업가치: 영업이익 평균 * 10(고정 PER)
        String per = StringUtil.isStringEmpty(req.getPer()) ? "10" : StringUtil.defaultString(req.getPer());
        requestContext.setAttribute(RequestContextConst.PER, per);
        final String businessValue = CalUtil.multi(operatingProfitAvg, per);
        if(log.isDebugEnabled()) log.debug("1. 사업가치 = {}", businessValue);
        requestContext.setAttribute(RequestContextConst.계산_사업가치, businessValue);

        // 재산가치: 유동자산 - (유동부채 * 유동비율) + 투자자산
        final String liabilityProduct = CalUtil.multi(liabilitiesTotal, currentRatio);
        final String assetDifference = CalUtil.sub(assetsTotal, liabilityProduct);
        final String assetValue = CalUtil.add(assetDifference, investmentAssets);
        if(log.isDebugEnabled()) log.debug("2. 재산가치 = {}", assetValue);
        requestContext.setAttribute(RequestContextConst.계산_재산가치, assetValue);

        // 부채: 고정부채 (비유동부채)
        final String debt = fixedLiabilities;
        if(log.isDebugEnabled()) log.debug("3. 부채 = {}", debt);
        requestContext.setAttribute(RequestContextConst.계산_부채, debt);

        // 기업가치: 사업가치 + 재산가치 - 부채
        final String companyValue = CalUtil.sub(CalUtil.add(businessValue, assetValue), debt);
        if(log.isDebugEnabled()) log.debug("4. 기업가치 = {}", companyValue);
        requestContext.setAttribute(RequestContextConst.계산_기업가치, companyValue);

        // 3. 한 주 가격 계산: (기업가치 * 단위복원) / 발행주식수
        return CalUtil.divide(
                CalUtil.multi(companyValue, req.getUnit()),
                issuedShares,
                RoundingMode.HALF_EVEN
        );
    }

    /**
     * 영업이익 평균을 계산한다.
     *
     * @param profitPrePre 전전기 영업이익
     * @param profitPre    전기 영업이익
     * @param profitCurrent 당기 영업이익
     * @return 영업이익 평균 값
     */
    private String calOperatingProfitAvg(String profitPrePre, String profitPre, String profitCurrent) {
        // 전전기 + 전기 + 당기를 한 번에 더한 후 평균 계산
        final String sum = CalUtil.add(CalUtil.add(profitPrePre, profitPre), profitCurrent);
        final String avg = CalUtil.divide(sum, "3", 2, RoundingMode.HALF_UP);

        requestContext.setAttribute(RequestContextConst.영업이익_합계, sum);
        requestContext.setAttribute(RequestContextConst.영업이익_평균, avg);

        return avg;
    }

}