package com.finance.dart.api.abroad.dto.fmp.financialratios;

import lombok.Data;

@Data
public class FinancialRatiosResDto {

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

    /** 매출총이익률 */
    private Double grossProfitMargin;

    /** EBIT 마진 */
    private Double ebitMargin;

    /** EBITDA 마진 */
    private Double ebitdaMargin;

    /** 영업이익률 */
    private Double operatingProfitMargin;

    /** 법인세차감전이익률 */
    private Double pretaxProfitMargin;

    /** 계속사업부문 순이익률 */
    private Double continuousOperationsProfitMargin;

    /** 순이익률 */
    private Double netProfitMargin;

    /** 최종 순이익률 (Bottom Line Profit Margin) */
    private Double bottomLineProfitMargin;

    /** 매출채권회전율 */
    private Double receivablesTurnover;

    /** 매입채무회전율 */
    private Double payablesTurnover;

    /** 재고자산회전율 */
    private Double inventoryTurnover;

    /** 고정자산회전율 */
    private Double fixedAssetTurnover;

    /** 총자산회전율 */
    private Double assetTurnover;

    /** 유동비율 */
    private Double currentRatio;

    /** 당좌비율 */
    private Double quickRatio;

    /** 지급능력비율 (Solvency Ratio) */
    private Double solvencyRatio;

    /** 현금비율 */
    private Double cashRatio;

    /** PER (주가수익비율) */
    private Double priceToEarningsRatio;

    /** PEG (주가수익성장비율) */
    private Double priceToEarningsGrowthRatio;

    /** Forward PEG */
    private Double forwardPriceToEarningsGrowthRatio;

    /** PBR (주가순자산비율) */
    private Double priceToBookRatio;

    /** PSR (주가매출비율) */
    private Double priceToSalesRatio;

    /** P/FCF (주가잉여현금흐름비율) */
    private Double priceToFreeCashFlowRatio;

    /** P/OCF (주가영업현금흐름비율) */
    private Double priceToOperatingCashFlowRatio;

    /** 부채/자산 비율 */
    private Double debtToAssetsRatio;

    /** 부채/자본 비율 */
    private Double debtToEquityRatio;

    /** 부채/자본총계 비율 */
    private Double debtToCapitalRatio;

    /** 장기부채/자본총계 비율 */
    private Double longTermDebtToCapitalRatio;

    /** 재무레버리지 비율 */
    private Double financialLeverageRatio;

    /** 운전자본회전율 */
    private Double workingCapitalTurnoverRatio;

    /** 영업현금흐름비율 */
    private Double operatingCashFlowRatio;

    /** 영업현금흐름/매출 비율 */
    private Double operatingCashFlowSalesRatio;

    /** 자유현금흐름/영업현금흐름 비율 */
    private Double freeCashFlowOperatingCashFlowRatio;

    /** 부채상환능력비율 (DSCR) */
    private Double debtServiceCoverageRatio;

    /** 이자보상배율 */
    private Double interestCoverageRatio;

    /** 단기영업현금흐름커버리지비율 */
    private Double shortTermOperatingCashFlowCoverageRatio;

    /** 영업현금흐름 커버리지비율 */
    private Double operatingCashFlowCoverageRatio;

    /** 자본적지출 커버리지비율 */
    private Double capitalExpenditureCoverageRatio;

    /** 배당 및 CAPEX 커버리지비율 */
    private Double dividendPaidAndCapexCoverageRatio;

    /** 배당성향 (배당금/순이익) */
    private Double dividendPayoutRatio;

    /** 배당수익률 */
    private Double dividendYield;

    /** 배당수익률(%) */
    private Double dividendYieldPercentage;

    /** 주당 매출액 */
    private Double revenuePerShare;

    /** 주당 순이익 */
    private Double netIncomePerShare;

    /** 주당 이자부채 */
    private Double interestDebtPerShare;

    /** 주당 현금 */
    private Double cashPerShare;

    /** 주당 장부가치 */
    private Double bookValuePerShare;

    /** 주당 유형장부가치 */
    private Double tangibleBookValuePerShare;

    /** 주당 자기자본 */
    private Double shareholdersEquityPerShare;

    /** 주당 영업현금흐름 */
    private Double operatingCashFlowPerShare;

    /** 주당 자본적지출 */
    private Double capexPerShare;

    /** 주당 자유현금흐름 */
    private Double freeCashFlowPerShare;

    /** 순이익 / 법인세차감전이익 */
    private Double netIncomePerEBT;

    /** 법인세차감전이익 / EBIT */
    private Double ebtPerEbit;

    /** 주가/공정가치 비율 */
    private Double priceToFairValue;

    /** 부채 / 시가총액 비율 */
    private Double debtToMarketCap;

    /** 유효세율 */
    private Double effectiveTaxRate;

    /** EV/EBITDA 배수 */
    private Double enterpriseValueMultiple;
}
