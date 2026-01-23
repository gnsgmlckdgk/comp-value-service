package com.finance.dart.cointrade.dto.upbit;

import lombok.Data;

/**
 * 캔들(봉) 조회 공통 응답 DTO
 */
@Data
public class CandleCommonResDto {

    /**
     * 마켓명
     */
    private String market;

    /**
     * 캔들 기준 시각 (UTC 기준)
     * 포맷: yyyy-MM-dd'T'HH:mm:ss
     */
    private String candle_date_time_utc;

    /**
     * 캔들 기준 시각 (KST 기준)
     * 포맷: yyyy-MM-dd'T'HH:mm:ss
     */
    private String candle_date_time_kst;

    /**
     * 시가
     */
    private Double opening_price;

    /**
     * 고가
     */
    private Double high_price;

    /**
     * 저가
     */
    private Double low_price;

    /**
     * 종가
     */
    private Double trade_price;

    /**
     * 해당 캔들에서 마지막 틱이 저장된 시각
     */
    private Long timestamp;

    /**
     * 누적 거래 금액
     */
    private Double candle_acc_trade_price;

    /**
     * 누적 거래량
     */
    private Double candle_acc_trade_volume;
}
