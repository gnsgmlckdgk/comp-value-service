package com.finance.dart.api.abroad.dto.fmp.keymetrics;

import lombok.Data;

@Data
public class KeyMetricsResDto {

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

    /** 시가총액 (Market Capitalization) */
    private Long marketCap;

    /** 기업가치 (Enterprise Value) */
    private Long enterpriseValue;

    /** EV / 매출 */
    private Double evToSales;

    /** EV / 영업현금흐름 */
    private Double evToOperatingCashFlow;

    /** EV / 잉여현금흐름 */
    private Double evToFreeCashFlow;

    /** EV / EBITDA */
    private Double evToEBITDA;

    /** 순부채 / EBITDA */
    private Double netDebtToEBITDA;

    /** 유동비율 (Current Ratio) */
    private Double currentRatio;

    /** 수익의 질 (Income Quality = OCF / Net Income) */
    private Double incomeQuality;

    /** 그레이엄 수 (Graham Number) */
    private Double grahamNumber;

    /** 그레이엄 순유동자산 가치 (Graham Net-Net) */
    private Double grahamNetNet;

    /** 세금부담률 (Tax Burden) */
    private Double taxBurden;

    /** 이자부담률 (Interest Burden) */
    private Double interestBurden;

    /** 운전자본 (Working Capital) */
    private Long workingCapital;

    /** 투하자본 (Invested Capital) */
    private Long investedCapital;

    /** 자산수익률 (ROA) */
    private Double returnOnAssets;

    /** 영업자산수익률 */
    private Double operatingReturnOnAssets;

    /** 유형자산수익률 (Return on Tangible Assets) */
    private Double returnOnTangibleAssets;

    /** 자기자본수익률 (ROE) */
    private Double returnOnEquity;

    /** 투하자본수익률 (ROIC) */
    private Double returnOnInvestedCapital;

    /** 사용자본수익률 (ROCE) */
    private Double returnOnCapitalEmployed;

    /** 이익수익률 (Earnings Yield = EPS / Price) */
    private Double earningsYield;

    /** 잉여현금흐름 수익률 */
    private Double freeCashFlowYield;

    /** CapEx / 영업현금흐름 */
    private Double capexToOperatingCashFlow;

    /** CapEx / 감가상각비 */
    private Double capexToDepreciation;

    /** CapEx / 매출 */
    private Double capexToRevenue;

    /** 판매관리비 / 매출 */
    private Double salesGeneralAndAdministrativeToRevenue;

    /** 연구개발비 / 매출 */
    private Double researchAndDevelopementToRevenue;

    /** 주식보상비용 / 매출 */
    private Double stockBasedCompensationToRevenue;

    /** 무형자산 / 총자산 */
    private Double intangiblesToTotalAssets;

    /** 평균 매출채권 */
    private Long averageReceivables;

    /** 평균 매입채무 */
    private Long averagePayables;

    /** 평균 재고자산 */
    private Long averageInventory;

    /** 매출채권회전일수 (DSO) */
    private Double daysOfSalesOutstanding;

    /** 매입채무회전일수 (DPO) */
    private Double daysOfPayablesOutstanding;

    /** 재고자산회전일수 (DIO) */
    private Double daysOfInventoryOutstanding;

    /** 영업주기 (Operating Cycle) */
    private Double operatingCycle;

    /** 현금전환주기 (CCC) */
    private Double cashConversionCycle;

    /** 자기자본 잉여현금흐름 (FCFE) */
    private Long freeCashFlowToEquity;

    /** 기업 잉여현금흐름 (FCFF) */
    private Double freeCashFlowToFirm;

    /** 유형자산 가치 */
    private Long tangibleAssetValue;

    /** 순운전자산 가치 (NCAV) */
    private Long netCurrentAssetValue;
}
