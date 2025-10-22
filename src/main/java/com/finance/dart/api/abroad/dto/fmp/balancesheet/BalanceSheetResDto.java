package com.finance.dart.api.abroad.dto.fmp.balancesheet;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class BalanceSheetResDto {

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

    /** 현금 및 현금성 자산 */
    private Long cashAndCashEquivalents;

    /** 단기투자자산 */
    private Long shortTermInvestments;

    /** 현금 및 단기투자 합계 */
    private Long cashAndShortTermInvestments;

    /** 순매출채권 (Net Receivables) */
    private Long netReceivables;

    /** 매출채권 (Accounts Receivable) */
    private Long accountsReceivables;

    /** 기타 채권 (Other Receivables) */
    private Long otherReceivables;

    /** 재고자산 */
    private Long inventory;

    /** 선수금·선급비용 등 (Prepaids) */
    private Long prepaids;

    /** 기타 유동자산 */
    private Long otherCurrentAssets;

    /** 유동자산 합계 */
    private Long totalCurrentAssets;

    /** 유형자산 (PPE, 순액) */
    private Long propertyPlantEquipmentNet;

    /** 영업권 (Goodwill) */
    private Long goodwill;

    /** 무형자산 */
    private Long intangibleAssets;

    /** 영업권 + 무형자산 합계 */
    private Long goodwillAndIntangibleAssets;

    /** 장기투자자산 */
    private Long longTermInvestments;

    /** 법인세 자산 (Deferred/Tax Assets) */
    private Long taxAssets;

    /** 기타 비유동자산 */
    private Long otherNonCurrentAssets;

    /** 비유동자산 합계 */
    private Long totalNonCurrentAssets;

    /** 기타 자산 */
    private Long otherAssets;

    /** 총자산 */
    private Long totalAssets;

    /** 총 미지급금 (Payables) */
    private Long totalPayables;

    /** 매입채무 (Accounts Payable) */
    private Long accountPayables;

    /** 기타 미지급금 */
    private Long otherPayables;

    /** 미지급비용 (Accrued Expenses) */
    private Long accruedExpenses;

    /** 단기차입금 */
    private Long shortTermDebt;

    /** 현재분 금융리스 부채 */
    private Long capitalLeaseObligationsCurrent;

    /** 법인세 부채 (단기) */
    private Long taxPayables;

    /** 이연수익 (단기) */
    private Long deferredRevenue;

    /** 기타 유동부채 */
    private Long otherCurrentLiabilities;

    /** 유동부채 합계 */
    private Long totalCurrentLiabilities;

    /** 장기차입금 */
    private Long longTermDebt;

    /** 이연수익 (장기) */
    private Long deferredRevenueNonCurrent;

    /** 이연법인세 부채 (장기) */
    private Long deferredTaxLiabilitiesNonCurrent;

    /** 기타 비유동부채 */
    private Long otherNonCurrentLiabilities;

    /** 비유동부채 합계 */
    private Long totalNonCurrentLiabilities;

    /** 기타 부채 */
    private Long otherLiabilities;

    /** 금융리스 부채 (비유동) */
    private Long capitalLeaseObligations;

    /** 총부채 */
    private Long totalLiabilities;

    /** 자기주식 (Treasury Stock) */
    private Long treasuryStock;

    /** 우선주 (Preferred Stock) */
    private Long preferredStock;

    /** 보통주 (Common Stock) */
    private Long commonStock;

    /** 이익잉여금 */
    private Long retainedEarnings;

    /** 추가납입자본 (APIC) */
    private Long additionalPaidInCapital;

    /** 기타포괄손익누계액 */
    private Long accumulatedOtherComprehensiveIncomeLoss;

    /** 기타 자본 */
    private Long otherTotalStockholdersEquity;

    /** 총주주지분 */
    private Long totalStockholdersEquity;

    /** 총자본 */
    private Long totalEquity;

    /** 비지배지분 (Minority Interest) */
    private Long minorityInterest;

    /** 총부채와 총자본 합계 (자산총계와 일치해야 함) */
    private Long totalLiabilitiesAndTotalEquity;

    /** 총투자자산 */
    private Long totalInvestments;

    /** 총부채 (단기+장기) */
    private Long totalDebt;

    /** 순부채 (총부채 - 현금) */
    private Long netDebt;

    /**
     * 모든 금액(Long 타입) 필드를 입력된 환율로 현지통화에서 USD로 환산합니다.
     * <p>
     * 예시:<br>
     * CNYUSD 환율이 0.14 일 때 (1위안 = 0.14달러)<br>
     * - 1,000,000위안 → 140,000달러<br>
     * - 10,000,000,000위안 → 1,400,000,000달러
     * </p>
     *
     * @param rate 적용할 환율 (예: 0.14)
     */
    @JsonIgnore
    public void applyExchangeRate(double rate) {
        cashAndCashEquivalents = apply(cashAndCashEquivalents, rate);
        shortTermInvestments = apply(shortTermInvestments, rate);
        cashAndShortTermInvestments = apply(cashAndShortTermInvestments, rate);
        netReceivables = apply(netReceivables, rate);
        accountsReceivables = apply(accountsReceivables, rate);
        otherReceivables = apply(otherReceivables, rate);
        inventory = apply(inventory, rate);
        prepaids = apply(prepaids, rate);
        otherCurrentAssets = apply(otherCurrentAssets, rate);
        totalCurrentAssets = apply(totalCurrentAssets, rate);
        propertyPlantEquipmentNet = apply(propertyPlantEquipmentNet, rate);
        goodwill = apply(goodwill, rate);
        intangibleAssets = apply(intangibleAssets, rate);
        goodwillAndIntangibleAssets = apply(goodwillAndIntangibleAssets, rate);
        longTermInvestments = apply(longTermInvestments, rate);
        taxAssets = apply(taxAssets, rate);
        otherNonCurrentAssets = apply(otherNonCurrentAssets, rate);
        totalNonCurrentAssets = apply(totalNonCurrentAssets, rate);
        otherAssets = apply(otherAssets, rate);
        totalAssets = apply(totalAssets, rate);
        totalPayables = apply(totalPayables, rate);
        accountPayables = apply(accountPayables, rate);
        otherPayables = apply(otherPayables, rate);
        accruedExpenses = apply(accruedExpenses, rate);
        shortTermDebt = apply(shortTermDebt, rate);
        capitalLeaseObligationsCurrent = apply(capitalLeaseObligationsCurrent, rate);
        taxPayables = apply(taxPayables, rate);
        deferredRevenue = apply(deferredRevenue, rate);
        otherCurrentLiabilities = apply(otherCurrentLiabilities, rate);
        totalCurrentLiabilities = apply(totalCurrentLiabilities, rate);
        longTermDebt = apply(longTermDebt, rate);
        deferredRevenueNonCurrent = apply(deferredRevenueNonCurrent, rate);
        deferredTaxLiabilitiesNonCurrent = apply(deferredTaxLiabilitiesNonCurrent, rate);
        otherNonCurrentLiabilities = apply(otherNonCurrentLiabilities, rate);
        totalNonCurrentLiabilities = apply(totalNonCurrentLiabilities, rate);
        otherLiabilities = apply(otherLiabilities, rate);
        capitalLeaseObligations = apply(capitalLeaseObligations, rate);
        totalLiabilities = apply(totalLiabilities, rate);
        treasuryStock = apply(treasuryStock, rate);
        preferredStock = apply(preferredStock, rate);
        commonStock = apply(commonStock, rate);
        retainedEarnings = apply(retainedEarnings, rate);
        additionalPaidInCapital = apply(additionalPaidInCapital, rate);
        accumulatedOtherComprehensiveIncomeLoss = apply(accumulatedOtherComprehensiveIncomeLoss, rate);
        otherTotalStockholdersEquity = apply(otherTotalStockholdersEquity, rate);
        totalStockholdersEquity = apply(totalStockholdersEquity, rate);
        totalEquity = apply(totalEquity, rate);
        minorityInterest = apply(minorityInterest, rate);
        totalLiabilitiesAndTotalEquity = apply(totalLiabilitiesAndTotalEquity, rate);
        totalInvestments = apply(totalInvestments, rate);
        totalDebt = apply(totalDebt, rate);
        netDebt = apply(netDebt, rate);
    }

    private Long apply(Long value, double rate) {
        if (value == null) return null;
        return Math.round(value * rate);
    }
}