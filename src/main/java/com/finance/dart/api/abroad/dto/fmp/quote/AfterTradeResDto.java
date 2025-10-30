package com.finance.dart.api.abroad.dto.fmp.quote;

import lombok.Data;

@Data
public class AfterTradeResDto {

    /** 종목 티커 (예: AAPL) */
    private String symbol;

    /** 체결 가격 (Price) */
    private Double price;

    /** 체결 수량 (Trade Size) */
    private Long tradeSize;

    /** 체결 시각 (Timestamp, 밀리초 단위) */
    private Long timestamp;

}
