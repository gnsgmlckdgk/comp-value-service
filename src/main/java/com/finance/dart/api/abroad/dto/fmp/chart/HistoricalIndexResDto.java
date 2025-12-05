package com.finance.dart.api.abroad.dto.fmp.chart;

import lombok.Data;

/**
 * 거래소 인덱스 차트 데이터 응답 DTO
 * FMP API는 배열 형태로 응답
 */
@Data
public class HistoricalIndexResDto {

    /** 인덱스 심볼 */
    private String symbol;

    /** 날짜 (YYYY-MM-DD) */
    private String date;

    /** 시가 */
    private Double open;

    /** 고가 */
    private Double high;

    /** 저가 */
    private Double low;

    /** 종가 */
    private Double close;

    /** 거래량 */
    private Long volume;

    /** 변동금액 */
    private Double change;

    /** 변동률 (%) */
    private Double changePercent;

    /** VWAP (Volume Weighted Average Price) */
    private Double vwap;
}
