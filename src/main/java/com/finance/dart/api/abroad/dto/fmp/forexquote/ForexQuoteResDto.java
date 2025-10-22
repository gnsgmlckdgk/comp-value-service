package com.finance.dart.api.abroad.dto.fmp.forexquote;

import lombok.Data;

/**
 * 외환 시세 응답 DTO
 */
@Data
public class ForexQuoteResDto {

    /** 통화쌍 심볼 (예: EURUSD) */
    private String symbol;

    /** 통화쌍 이름 (예: EUR/USD) */
    private String name;

    /** 현재 환율 (1유로(EUR) = 1.16041달러(USD)) */
    private Double price;

    /** 전일 대비 변동률 (%) */
    private Double changePercentage;

    /** 전일 대비 변동 금액 */
    private Double change;

    /** 거래량 */
    private Long volume;

    /** 일중 최저가 */
    private Double dayLow;

    /** 일중 최고가 */
    private Double dayHigh;

    /** 연중 최고가 */
    private Double yearHigh;

    /** 연중 최저가 */
    private Double yearLow;

    /** 시가총액 (해당 안 될 수도 있음) */
    private Double marketCap;

    /** 50일 이동평균가 */
    private Double priceAvg50;

    /** 200일 이동평균가 */
    private Double priceAvg200;

    /** 거래소 이름 (예: FOREX) */
    private String exchange;

    /** 시가 (Opening Price) */
    private Double open;

    /** 전일 종가 (Previous Close) */
    private Double previousClose;

    /** 타임스탬프 (Unix Epoch Time, 초 단위) */
    private Long timestamp;
}
