package com.finance.dart.api.abroad.dto.fmp.financialratios;

import lombok.Data;

@Data
public class FinancialRatiosTTM_ResDto {

    /** 종목 티커 (예: AAPL) */
    private String symbol;

    /** 매출총이익률 (Gross Profit Margin, TTM) */
    private Double grossProfitMarginTTM;

    /** EBIT 마진 (TTM) */
    private Double ebitMarginTTM;

    /** EBITDA 마진 (TTM) */
    private Double ebitdaMarginTTM;

    /** 영업이익률 (TTM) */
    private Double operatingProfitMarginTTM;

    /** 법인세차감전이익률 (Pretax Profit Margin, TTM) */
    private Double pretaxProfitMarginTTM;

    /** 계속사업부문 순이익률 (TTM) */
    private Double continuousOperationsProfitMarginTTM;

    /** 순이익률 (TTM) */
    private Double netProfitMarginTTM;

    /** 최종 순이익률 (Bottom Line Profit Margin, TTM) */
    private Double bottomLineProfitMarginTTM;

    /** 매출채권회전율 (TTM) */
    private Double receivablesTurnoverTTM;

    /** 매입채무회전율 (TTM) */
    private Double payablesTurnoverTTM;

    /** 재고자산회전율 (TTM) */
    private Double inventoryTurnoverTTM;

    /** 고정자산회전율 (TTM) */
    private Double fixedAssetTurnoverTTM;

    /** 총자산회전율 (TTM) */
    private Double assetTurnoverTTM;

    /** 유동비율 (TTM) */
    private Double currentRatioTTM;

    /** 당좌비율 (TTM) */
    private Double quickRatioTTM;

    /** 지급능력비율 (Solvency Ratio, TTM) */
    private Double solvencyRatioTTM;

    /** 현금비율 (TTM) */
    private Double cashRatioTTM;

    /** PER (주가수익비율, TTM) */
    private Double priceToEarningsRatioTTM;

    /** PEG (주가수익성장비율, TTM) */
    private Double priceToEarningsGrowthRatioTTM;

    /** Forward PEG (TTM) */
    private Double forwardPriceToEarningsGrowthRatioTTM;

    /** PBR (주가순자산비율, TTM) */
    private Double priceToBookRatioTTM;

    /** PSR (주가매출비율, TTM) */
    private Double priceToSalesRatioTTM;

    /** P/FCF (주가잉여현금흐름비율, TTM) */
    private Double priceToFreeCashFlowRatioTTM;

    /** P/OCF (주가영업현금흐름비율, TTM) */
    private Double priceToOperatingCashFlowRatioTTM;

    /** 부채/자산 비율 (TTM) */
    private Double debtToAssetsRatioTTM;

    /** 부채/자본 비율 (TTM) */
    private Double debtToEquityRatioTTM;

    /** 부채/자본총계 비율 (TTM) */
    private Double debtToCapitalRatioTTM;

    /** 장기부채/자본총계 비율 (TTM) */
    private Double longTermDebtToCapitalRatioTTM;

    /** 재무레버리지 비율 (TTM) */
    private Double financialLeverageRatioTTM;

    /** 운전자본회전율 (TTM) */
    private Double workingCapitalTurnoverRatioTTM;

    /** 영업현금흐름비율 (TTM) */
    private Double operatingCashFlowRatioTTM;

    /** 영업현금흐름/매출 비율 (TTM) */
    private Double operatingCashFlowSalesRatioTTM;

    /** 자유현금흐름/영업현금흐름 비율 (TTM) */
    private Double freeCashFlowOperatingCashFlowRatioTTM;

    /** 부채상환능력비율 (DSCR, TTM) */
    private Double debtServiceCoverageRatioTTM;

    /** 이자보상배율 (TTM) */
    private Double interestCoverageRatioTTM;

    /** 단기영업현금흐름커버리지비율 (TTM) */
    private Double shortTermOperatingCashFlowCoverageRatioTTM;

    /** 영업현금흐름 커버리지비율 (TTM) */
    private Double operatingCashFlowCoverageRatioTTM;

    /** 자본적지출 커버리지비율 (TTM) */
    private Double capitalExpenditureCoverageRatioTTM;

    /** 배당 및 CAPEX 커버리지비율 (TTM) */
    private Double dividendPaidAndCapexCoverageRatioTTM;

    /** 배당성향 (TTM) */
    private Double dividendPayoutRatioTTM;

    /** 배당수익률 (TTM) */
    private Double dividendYieldTTM;

    /** 기업가치 (Enterprise Value, TTM) */
    private Long enterpriseValueTTM;

    /** 주당 매출액 (TTM) */
    private Double revenuePerShareTTM;

    /** 주당 순이익 (TTM) */
    private Double netIncomePerShareTTM;

    /** 주당 이자부채 (TTM) */
    private Double interestDebtPerShareTTM;

    /** 주당 현금 (TTM) */
    private Double cashPerShareTTM;

    /** 주당 장부가치 (TTM) */
    private Double bookValuePerShareTTM;

    /** 주당 유형장부가치 (TTM) */
    private Double tangibleBookValuePerShareTTM;

    /** 주당 자기자본 (TTM) */
    private Double shareholdersEquityPerShareTTM;

    /** 주당 영업현금흐름 (TTM) */
    private Double operatingCashFlowPerShareTTM;

    /** 주당 자본적지출 (TTM) */
    private Double capexPerShareTTM;

    /** 주당 자유현금흐름 (TTM) */
    private Double freeCashFlowPerShareTTM;

    /** 순이익 / 법인세차감전이익 (TTM) */
    private Double netIncomePerEBTTTM;

    /** 법인세차감전이익 / EBIT (TTM) */
    private Double ebtPerEbitTTM;

    /** 주가/공정가치 비율 (TTM) */
    private Double priceToFairValueTTM;

    /** 부채 / 시가총액 비율 (TTM) */
    private Double debtToMarketCapTTM;

    /** 유효세율 (TTM) */
    private Double effectiveTaxRateTTM;

    /** EV/EBITDA 배수 (TTM) */
    private Double enterpriseValueMultipleTTM;
}
