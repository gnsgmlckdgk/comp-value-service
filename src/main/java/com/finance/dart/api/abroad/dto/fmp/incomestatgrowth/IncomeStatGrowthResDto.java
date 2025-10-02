package com.finance.dart.api.abroad.dto.fmp.incomestatgrowth;

import lombok.Data;

@Data
public class IncomeStatGrowthResDto {

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
    private Double growthRevenue;

    /** 매출원가 성장률 */
    private Double growthCostOfRevenue;

    /** 매출총이익 성장률 */
    private Double growthGrossProfit;

    /** 매출총이익률 성장 */
    private Double growthGrossProfitRatio;

    /** 연구개발비 성장률 */
    private Double growthResearchAndDevelopmentExpenses;

    /** 일반관리비 성장률 */
    private Double growthGeneralAndAdministrativeExpenses;

    /** 판매마케팅비 성장률 */
    private Double growthSellingAndMarketingExpenses;

    /** 기타비용 성장률 */
    private Double growthOtherExpenses;

    /** 영업비용 성장률 */
    private Double growthOperatingExpenses;

    /** 총비용 성장률 */
    private Double growthCostAndExpenses;

    /** 이자수익 성장률 */
    private Double growthInterestIncome;

    /** 이자비용 성장률 */
    private Double growthInterestExpense;

    /** 감가상각비 성장률 */
    private Double growthDepreciationAndAmortization;

    /** EBITDA 성장률 */
    private Double growthEBITDA;

    /** 영업이익 성장률 */
    private Double growthOperatingIncome;

    /** 세전이익 성장률 */
    private Double growthIncomeBeforeTax;

    /** 법인세 비용 성장률 */
    private Double growthIncomeTaxExpense;

    /** 순이익 성장률 */
    private Double growthNetIncome;

    /** 주당순이익(EPS) 성장률 */
    private Double growthEPS;

    /** 희석주당순이익(EPS Diluted) 성장률 */
    private Double growthEPSDiluted;

    /** 가중평균 발행주식수 성장률 */
    private Double growthWeightedAverageShsOut;

    /** 가중평균 발행주식수(희석주 포함) 성장률 */
    private Double growthWeightedAverageShsOutDil;

    /** EBIT 성장률 */
    private Double growthEBIT;

    /** 영업외수익 성장률 (이자 제외) */
    private Double growthNonOperatingIncomeExcludingInterest;

    /** 순이자수익 성장률 */
    private Double growthNetInterestIncome;

    /** 기타 수익/비용 합계 성장률 */
    private Double growthTotalOtherIncomeExpensesNet;

    /** 계속사업부문 순이익 성장률 */
    private Double growthNetIncomeFromContinuingOperations;

    /** 순이익 조정분 성장률 */
    private Double growthOtherAdjustmentsToNetIncome;

    /** 순이익 차감액 성장률 */
    private Double growthNetIncomeDeductions;
}
