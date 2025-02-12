package com.finance.dart.api.service;


import com.finance.dart.api.dto.StockValueManualReqDTO;
import com.finance.dart.common.util.CalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;


@Slf4j
@RequiredArgsConstructor
@Service
public class CalCompanyStockPerValueManualService {


    /**
     * 한주당 가치 계산
     * @param stockValueManualReqDTO
     * @return
     */
    public String calPerValue(StockValueManualReqDTO stockValueManualReqDTO) {

        log.debug("stockValueManualReqDTO = {}", stockValueManualReqDTO);

        // 영업이익 평균
        String operatingProfitAvg = calOperatingProfitAvg(
                stockValueManualReqDTO.getOperatingProfitPrePre(),
                stockValueManualReqDTO.getOperatingProfitPre(),
                stockValueManualReqDTO.getOperatingProfitCurrent()
                );
        log.debug("영업이익 평균 = {}", operatingProfitAvg);

        String assetsTotal = stockValueManualReqDTO.getCurrentAssetsTotal();            // 유동자산합계
        String liablitiesTotal = stockValueManualReqDTO.getCurrentLiabilitiesTotal();   // 유동부채합계
        String ratio = stockValueManualReqDTO.getCurrentRatio();                        // 유동비율
        String investmentAssets = stockValueManualReqDTO.getInvestmentAssets();         // 투자자산(비유동자산내)
        String fixedLiabilities = stockValueManualReqDTO.getFixedLiabilities();         // 고정부채(비유동부채)
        String issuedShares = stockValueManualReqDTO.getIssuedShares();                 // 발행주식수

        //@ 결과 계산
        //#1. 사업가치 ( 영업이익(전전기, 전기, 당기) 평균 * 10 )
        String 사업가치 = CalUtil.multi(operatingProfitAvg, "10");
        log.debug("1. 사업가치 = {}", 사업가치);

        //#2. 재산가치 ( 유동자산 - (유동부채 * 유동비율) + 투자자산 )
        String step01 = CalUtil.multi(liablitiesTotal, ratio);
        String step02 = CalUtil.sub(assetsTotal, step01);
        String 재산가치 = CalUtil.add(step02, investmentAssets);
        log.debug("2. 재산가치 = {}", 재산가치);

        //#3. 부채 ( 비유동부채(고정부채) )
        String 부채 = fixedLiabilities;
        log.debug("3. 부채 = {}", 부채);

        //#4. 기업가치
        // 사업가치 + 재산가치 - 부채
        String 기업가치 = CalUtil.sub(CalUtil.add(사업가치, 재산가치), 부채);
        log.debug("4. 기업가치 = {}", 기업가치);

        //#5. 한주 가격 계산
        // 기업가치(단위복원:실제가격) / 발행주식수
        return CalUtil.divide(CalUtil.multi(기업가치, stockValueManualReqDTO.getUnit()), issuedShares, RoundingMode.HALF_EVEN);
    }

    /**
     * 영업이익 평균 계산
     * @param profitPrePre
     * @param profitPre
     * @param profitCurrent
     * @return
     */
    private String calOperatingProfitAvg(String profitPrePre, String profitPre, String profitCurrent) {

        String operatingProfitSum = "0";
        String operatingProfitAvg = "0";
        // 전전기 + 전기 = A
        operatingProfitSum = CalUtil.add(profitPrePre, profitPre);
        // A + 당기
        operatingProfitSum = CalUtil.add(operatingProfitSum, profitCurrent);
        // 평균
        operatingProfitAvg = CalUtil.divide(operatingProfitSum, "3", 2, RoundingMode.HALF_UP);

        return operatingProfitAvg;
    }

}
