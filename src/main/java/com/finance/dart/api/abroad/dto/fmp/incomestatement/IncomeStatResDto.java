package com.finance.dart.api.abroad.dto.fmp.incomestatement;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    /**
     * 모든 금액 필드를 입력한 환율로 원화 등 현지 통화에서 달러(USD)로 변환합니다.
     * EPS 및 주식 수 관련 필드는 변환되지 않습니다.
     *
     * @param rate 적용할 환율 (예: 1원당 달러 값)
     *
     * 예시:
     * KRWUSD 환율이 0.00069 일 때 (1원 = 0.00069달러)
     * - 1,000,000원 → 690달러
     * - 10,000,000,000원 → 6,900,000달러
     */
    @JsonIgnore
    public void applyExchangeRate(double rate) {
        this.revenue = apply(this.revenue, rate);
        this.costOfRevenue = apply(this.costOfRevenue, rate);
        this.grossProfit = apply(this.grossProfit, rate);
        this.researchAndDevelopmentExpenses = apply(this.researchAndDevelopmentExpenses, rate);
        this.generalAndAdministrativeExpenses = apply(this.generalAndAdministrativeExpenses, rate);
        this.sellingAndMarketingExpenses = apply(this.sellingAndMarketingExpenses, rate);
        this.sellingGeneralAndAdministrativeExpenses = apply(this.sellingGeneralAndAdministrativeExpenses, rate);
        this.otherExpenses = apply(this.otherExpenses, rate);
        this.operatingExpenses = apply(this.operatingExpenses, rate);
        this.costAndExpenses = apply(this.costAndExpenses, rate);
        this.netInterestIncome = apply(this.netInterestIncome, rate);
        this.interestIncome = apply(this.interestIncome, rate);
        this.interestExpense = apply(this.interestExpense, rate);
        this.depreciationAndAmortization = apply(this.depreciationAndAmortization, rate);
        this.ebitda = apply(this.ebitda, rate);
        this.ebit = apply(this.ebit, rate);
        this.nonOperatingIncomeExcludingInterest = apply(this.nonOperatingIncomeExcludingInterest, rate);
        this.operatingIncome = apply(this.operatingIncome, rate);
        this.totalOtherIncomeExpensesNet = apply(this.totalOtherIncomeExpensesNet, rate);
        this.incomeBeforeTax = apply(this.incomeBeforeTax, rate);
        this.incomeTaxExpense = apply(this.incomeTaxExpense, rate);
        this.netIncomeFromContinuingOperations = apply(this.netIncomeFromContinuingOperations, rate);
        this.netIncomeFromDiscontinuedOperations = apply(this.netIncomeFromDiscontinuedOperations, rate);
        this.otherAdjustmentsToNetIncome = apply(this.otherAdjustmentsToNetIncome, rate);
        this.netIncome = apply(this.netIncome, rate);
        this.netIncomeDeductions = apply(this.netIncomeDeductions, rate);
        this.bottomLineNetIncome = apply(this.bottomLineNetIncome, rate);
    }

    private Long apply(Long value, double rate) {
        if (value == null) return null;
        return Math.round(value * rate);
    }

}
