package com.finance.dart.api.abroad.dto.fmp.incomestatement;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * FMP 영업이익 조회 응답 DTO
 */
@Data
public class IncomeStatResDto {

    /** 보고 기준일 */
    private String date;

    /** 종목 티커 (예: AAPL) */
    private String symbol;

    /** 보고 통화 단위 (예: USD) */
    private String reportedCurrency;

    /** SEC CIK (기업 식별자) */
    private String cik;

    /** 보고서 제출일 */
    private String filingDate;

    /** SEC 접수일/시각 */
    private String acceptedDate;

    /** 회계연도 (Fiscal Year) */
    private String fiscalYear;

    /** 기간 구분 (FY: 연간, Q1~Q4: 분기) */
    private String period;

    /** 매출 (Revenue) */
    private Long revenue;

    /** 매출원가 (Cost of Revenue) */
    private Long costOfRevenue;

    /** 매출총이익 (Gross Profit) */
    private Long grossProfit;

    /** 연구개발비 (R&D) */
    private Long researchAndDevelopmentExpenses;

    /** 일반관리비 (G&A) */
    private Long generalAndAdministrativeExpenses;

    /** 판매마케팅비 */
    private Long sellingAndMarketingExpenses;

    /** 판관비 (Selling, General & Administrative) */
    private Long sellingGeneralAndAdministrativeExpenses;

    /** 기타비용 */
    private Long otherExpenses;

    /** 영업비용 합계 */
    private Long operatingExpenses;

    /** 총비용 (Cost + Expenses) */
    private Long costAndExpenses;

    /** 순이자수익 (금융회사 주로 사용) */
    private Long netInterestIncome;

    /** 이자수익 */
    private Long interestIncome;

    /** 이자비용 */
    private Long interestExpense;

    /** 감가상각비 */
    private Long depreciationAndAmortization;

    /** EBITDA (세전·이자·감가상각전 이익) */
    private Long ebitda;

    /** EBIT (이자·세전이익) */
    private Long ebit;

    /** 영업외수익 (이자 제외) */
    private Long nonOperatingIncomeExcludingInterest;

    /** 영업이익 */
    private Long operatingIncome;

    /** 기타 수익/비용 합계 */
    private Long totalOtherIncomeExpensesNet;

    /** 법인세차감전순이익 (Pretax Income) */
    private Long incomeBeforeTax;

    /** 법인세 비용 */
    private Long incomeTaxExpense;

    /** 계속사업부문 순이익 */
    private Long netIncomeFromContinuingOperations;

    /** 중단사업부문 순이익 */
    private Long netIncomeFromDiscontinuedOperations;

    /** 순이익에 대한 기타 조정 */
    private Long otherAdjustmentsToNetIncome;

    /** 당기순이익 (Net Income) */
    private Long netIncome;

    /** 순이익 차감액 (비지배지분 등) */
    private Long netIncomeDeductions;

    /** 최종 순이익 (Bottom Line Net Income) */
    private Long bottomLineNetIncome;

    /** EPS (주당순이익, Basic) */
    private Double eps;

    /** 희석주당순이익 (Diluted EPS) */
    private Double epsDiluted;

    /** 가중평균 주식수 (보통주) */
    private Long weightedAverageShsOut;

    /** 가중평균 주식수 (희석주 포함) */
    @JsonProperty("weightedAverageShsOutDil")
    private Long weightedAverageShsOutDiluted;

}
