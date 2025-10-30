package com.finance.dart.api.abroad.dto.fmp.quote;

import lombok.Data;

@Data
public class StockQuoteResDto {

    /** 종목 티커 (예: AAPL) */
    private String symbol;

    /** 현재 주가 */
    private Double price;

    /** 전일 대비 변동 금액 */
    private Double change;

    /** 거래량 */
    private Long volume;
}
