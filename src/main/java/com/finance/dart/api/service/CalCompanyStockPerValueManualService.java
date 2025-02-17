package com.finance.dart.api.service;

import com.finance.dart.api.dto.StockValueManualReqDTO;
import com.finance.dart.common.util.CalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;

/**
 * 한 주당 가치 수동계산
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CalCompanyStockPerValueManualService {

    /**
     * 한 주당 가치를 계산한다.
     *
     * @param req 계산에 필요한 데이터가 담긴 StockValueManualReqDTO
     * @return 한 주 가격
     */
    public String calPerValue(StockValueManualReqDTO req) {
        log.debug("stockValueManualReqDTO = {}", req);

        // 1. 영업이익 평균 계산
        final String operatingProfitAvg = calOperatingProfitAvg(
                req.getOperatingProfitPrePre(),
                req.getOperatingProfitPre(),
                req.getOperatingProfitCurrent()
        );
        log.debug("영업이익 평균 = {}", operatingProfitAvg);

        // 요청 객체에서 필요한 값들을 final 변수로 할당
        final String assetsTotal = req.getCurrentAssetsTotal();             // 유동자산합계
        final String liabilitiesTotal = req.getCurrentLiabilitiesTotal();   // 유동부채합계
        final String currentRatio = req.getCurrentRatio();                  // 유동비율
        final String investmentAssets = req.getInvestmentAssets();          // 투자자산 (비유동자산 내)
        final String fixedLiabilities = req.getFixedLiabilities();          // 고정부채 (비유동부채)
        final String issuedShares = req.getIssuedShares();                  // 발행주식수

        // 2. 각 단계별 계산

        // 사업가치: 영업이익 평균 * 10
        final String businessValue = CalUtil.multi(operatingProfitAvg, "10");
        log.debug("1. 사업가치 = {}", businessValue);

        // 재산가치: 유동자산 - (유동부채 * 유동비율) + 투자자산
        final String liabilityProduct = CalUtil.multi(liabilitiesTotal, currentRatio);
        final String assetDifference = CalUtil.sub(assetsTotal, liabilityProduct);
        final String assetValue = CalUtil.add(assetDifference, investmentAssets);
        log.debug("2. 재산가치 = {}", assetValue);

        // 부채: 고정부채 (비유동부채)
        final String debt = fixedLiabilities;
        log.debug("3. 부채 = {}", debt);

        // 기업가치: 사업가치 + 재산가치 - 부채
        final String companyValue = CalUtil.sub(CalUtil.add(businessValue, assetValue), debt);
        log.debug("4. 기업가치 = {}", companyValue);

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
        return CalUtil.divide(sum, "3", 2, RoundingMode.HALF_UP);
    }
}