package com.finance.dart.api.abroad.dto.fmp.enterprisevalues;

import lombok.Data;

@Data
public class EnterpriseValuesResDto {

    /** 종목 티커 (예: AAPL) */
    private String symbol;

    /** 보고 기준일 */
    private String date;

    /** 주가 (Stock Price) */
    private Double stockPrice;

    /** 발행주식 수 */
    private Long numberOfShares;

    /** 시가총액 (Market Capitalization) */
    private Long marketCapitalization;

    /** 현금 및 현금성 자산 차감분 */
    private Long minusCashAndCashEquivalents;

    /** 총부채 가산분 */
    private Long addTotalDebt;

    /** 기업가치 (Enterprise Value) */
    private Long enterpriseValue;
}
