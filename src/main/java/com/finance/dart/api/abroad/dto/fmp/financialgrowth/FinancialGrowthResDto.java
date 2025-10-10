package com.finance.dart.api.abroad.dto.fmp.financialgrowth;

import lombok.Data;

@Data
public class FinancialGrowthResDto {

    /** 종목 티커 (예: AAPL) */
    private String symbol;

    /** 보고 기준일 */
    private String date;

    /** 회계연도 (Fiscal Year) */
    private String fiscalYear;

    /** 기간 구분 (FY: 연간, Q1~Q4: 분기) */
    private String period;

    /** 보고 통화 단위 (예: USD) */
    private String reportedCurrency;

    /** 매출 성장률 */
    private Double revenueGrowth;

    /** 매출총이익 성장률 */
    private Double grossProfitGrowth;

    /** EBIT 성장률 */
    private Double ebitgrowth;

    /** 영업이익 성장률 */
    private Double operatingIncomeGrowth;

    /** 순이익 성장률 */
    private Double netIncomeGrowth;

    /**
     * <pre>
     * EPS 성장률
     * 마이너스면 실제 역성장 (10%->5% X / 10%->-5% O)
     * </pre>
     */
    private Double epsgrowth;

    /** 희석 EPS 성장률 */
    private Double epsdilutedGrowth;

    /** 가중평균 주식수 성장률 */
    private Double weightedAverageSharesGrowth;

    /** 가중평균 주식수(희석 포함) 성장률 */
    private Double weightedAverageSharesDilutedGrowth;

    /** 주당 배당금 성장률 */
    private Double dividendsPerShareGrowth;

    /** 영업현금흐름 성장률 */
    private Double operatingCashFlowGrowth;

    /** 매출채권 성장률 */
    private Double receivablesGrowth;

    /** 재고자산 성장률 */
    private Double inventoryGrowth;

    /** 자산총계 성장률 */
    private Double assetGrowth;

    /** 주당 순자산(장부가치) 성장률 */
    private Double bookValueperShareGrowth;

    /** 부채 성장률 */
    private Double debtGrowth;

    /** 연구개발비 성장률 */
    private Double rdexpenseGrowth;

    /** 판관비 성장률 */
    private Double sgaexpensesGrowth;

    /** 잉여현금흐름 성장률 */
    private Double freeCashFlowGrowth;

    /** 10년간 주당 매출 성장률 */
    private Double tenYRevenueGrowthPerShare;

    /** 5년간 주당 매출 성장률 */
    private Double fiveYRevenueGrowthPerShare;

    /** 3년간 주당 매출 성장률 */
    private Double threeYRevenueGrowthPerShare;

    /** 10년간 주당 영업현금흐름 성장률 */
    private Double tenYOperatingCFGrowthPerShare;

    /** 5년간 주당 영업현금흐름 성장률 */
    private Double fiveYOperatingCFGrowthPerShare;

    /** 3년간 주당 영업현금흐름 성장률 */
    private Double threeYOperatingCFGrowthPerShare;

    /** 10년간 주당 순이익 성장률 */
    private Double tenYNetIncomeGrowthPerShare;

    /** 5년간 주당 순이익 성장률 */
    private Double fiveYNetIncomeGrowthPerShare;

    /** 3년간 주당 순이익 성장률 */
    private Double threeYNetIncomeGrowthPerShare;

    /** 10년간 주당 자기자본 성장률 */
    private Double tenYShareholdersEquityGrowthPerShare;

    /** 5년간 주당 자기자본 성장률 */
    private Double fiveYShareholdersEquityGrowthPerShare;

    /** 3년간 주당 자기자본 성장률 */
    private Double threeYShareholdersEquityGrowthPerShare;

    /** 10년간 주당 배당금 성장률 */
    private Double tenYDividendperShareGrowthPerShare;

    /** 5년간 주당 배당금 성장률 */
    private Double fiveYDividendperShareGrowthPerShare;

    /** 3년간 주당 배당금 성장률 */
    private Double threeYDividendperShareGrowthPerShare;

    /** EBITDA 성장률 (null 가능) */
    private Double ebitdaGrowth;

    /** 자본적지출(CapEx) 성장률 (null 가능) */
    private Double growthCapitalExpenditure;

    /** 10년간 주당 최종순이익 성장률 (null 가능) */
    private Double tenYBottomLineNetIncomeGrowthPerShare;

    /** 5년간 주당 최종순이익 성장률 (null 가능) */
    private Double fiveYBottomLineNetIncomeGrowthPerShare;

    /** 3년간 주당 최종순이익 성장률 (null 가능) */
    private Double threeYBottomLineNetIncomeGrowthPerShare;

}
