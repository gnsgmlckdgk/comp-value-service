package com.finance.dart.api.common.dto.evaluation;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.finance.dart.api.common.dto.CompanySharePriceResultDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 종목 평가 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "symbol", "companyName", "currentPrice", "fairValue", "priceDifference",
    "priceGapPercent", "totalScore", "grade", "recommendation",
    "peg", "per", "sector", "industry", "beta", "exchange", "country", "marketCap",
    "step1Score", "step2Score", "step3Score", "step4Score",
    "stepDetails", "resultDetail"
})
public class StockEvaluationResponse {

    /**
     * 심볼 (티커)
     */
    private String symbol;

    /**
     * 기업명
     */
    private String companyName;

    /**
     * 현재 가격
     */
    private String currentPrice;

    /**
     * 주당 가치 (적정가)
     */
    private String fairValue;

    /**
     * 가격 차이 (주당가치 - 현재가격)
     */
    private String priceDifference;

    /**
     * 가격 차이 비율 (%)
     */
    private String priceGapPercent;

    /**
     * 총 점수 (100점 만점)
     */
    private double totalScore;

    /**
     * 등급 (S, A, B, C, D, F)
     */
    private String grade;

    /**
     * 투자 추천도
     */
    private String recommendation;

    /**
     * PEG
     */
    private String peg;

    /**
     * PER
     */
    private String per;

    /**
     * 섹터
     */
    private String sector;

    /**
     * 산업군
     */
    private String industry;

    /**
     * 베타
     */
    private String beta;

    /**
     * 거래소
     */
    private String exchange;

    /**
     * 국가
     */
    private String country;

    /**
     * 시가총액
     */
    private String marketCap;

    /**
     * Step 1 점수
     */
    private double step1Score;

    /**
     * Step 2 점수
     */
    private double step2Score;

    /**
     * Step 3 점수
     */
    private double step3Score;

    /**
     * Step 4 점수
     */
    private double step4Score;

    /**
     * 각 Step별 상세 평가 정보
     */
    private List<StepEvaluationDetail> stepDetails;

    /**
     * 상세 정보 (CompanySharePriceResultDetail)
     */
    private CompanySharePriceResultDetail resultDetail;

}
