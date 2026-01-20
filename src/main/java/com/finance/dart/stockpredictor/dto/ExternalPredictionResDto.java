package com.finance.dart.stockpredictor.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 외부 예측 서비스로부터 받는 응답 DTO
 */
@Getter
@Setter
@ToString
public class ExternalPredictionResDto {

    /** 티커 심볼 */
    private String ticker;

    /** 현재가 */
    @JsonProperty("current_price")
    private String currentPrice;

    /** 1주일 내 예측 최고가 */
    @JsonProperty("predicted_high_1w")
    private String predictedHigh1w;

    /** 상승 여력 (%) */
    @JsonProperty("upside_percent")
    private String upsidePercent;

    /** 데이터 소스 (database: DB 저장된 데이터, realtime: 실시간 예측 데이터) */
    private String source;

    /** 에러 메시지 (오류 발생 시) */
    private String error;
}
