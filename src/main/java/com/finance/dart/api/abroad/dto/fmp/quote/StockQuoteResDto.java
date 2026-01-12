package com.finance.dart.api.abroad.dto.fmp.quote;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StockQuoteResDto {

    /** 종목 티커 (예: AAPL) */
    private String symbol;

    /** 종목명 */
    private String name;

    /** 현재가 */
    private Double price;

    /** 변동률 (%) */
    private Double changePercentage;

    /** 전일 대비 변동 금액 */
    private Double change;

    /** 거래량 */
    private Long volume;

    /** 일중 최저가 */
    private Double dayLow;

    /** 일중 최고가 */
    private Double dayHigh;

    /** 52주 최고가 */
    private Double yearHigh;

    /** 52주 최저가 */
    private Double yearLow;

    /** 시가총액 */
    private Long marketCap;

    /** 50일 평균가 */
    private Double priceAvg50;

    /** 200일 평균가 */
    private Double priceAvg200;

    /** 거래소 (예: NASDAQ) */
    private String exchange;

    /** 시가 */
    private Double open;

    /** 전일 종가 */
    private Double previousClose;

    /** 타임스탬프 (Unix epoch seconds) */
    private Long timestamp;
}
