package com.finance.dart.api.abroad.dto.fmp.analystestimates;

import lombok.Data;

@Data
public class AnalystEstimatesResDto {

    /** 종목 티커 */
    private String symbol;

    /** 기준일 */
    private String date;

    /** 추정 매출 평균 */
    private Long estimatedRevenueAvg;

    /** 추정 매출 최저 */
    private Long estimatedRevenueLow;

    /** 추정 매출 최고 */
    private Long estimatedRevenueHigh;

    /** 추정 EPS 평균 */
    private Double estimatedEpsAvg;

    /** 추정 EPS 최저 */
    private Double estimatedEpsLow;

    /** 추정 EPS 최고 */
    private Double estimatedEpsHigh;

    /** 추정 순이익 평균 */
    private Long estimatedNetIncomeAvg;

    /** 추정 순이익 최저 */
    private Long estimatedNetIncomeLow;

    /** 추정 순이익 최고 */
    private Long estimatedNetIncomeHigh;

    /** 추정 EBITDA 평균 */
    private Long estimatedEbitdaAvg;

    /** 추정 EBITDA 최저 */
    private Long estimatedEbitdaLow;

    /** 추정 EBITDA 최고 */
    private Long estimatedEbitdaHigh;

    /** 추정 SGA 비용 평균 */
    private Long estimatedSgaExpenseAvg;

    /** 애널리스트 수 */
    private Integer numberAnalystEstimatedRevenue;

    /** EPS 추정 애널리스트 수 */
    private Integer numberAnalystsEstimatedEps;

}
