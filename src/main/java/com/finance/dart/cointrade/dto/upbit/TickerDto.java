package com.finance.dart.cointrade.dto.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 페어 단위 현재가 조회 응답 DTO
 * https://api.upbit.com/v1/ticker
 */
@Getter
@Setter
@ToString
public class TickerDto {

    /**
     * 종목 코드 (예: KRW-BTC)
     */
    @JsonProperty("market")
    @SerializedName("market")
    private String market;

    /**
     * 최근 거래 일자 (UTC 기준, 포맷: yyyyMMdd)
     */
    @JsonProperty("trade_date")
    @SerializedName("trade_date")
    private String tradeDate;

    /**
     * 최근 거래 시각 (UTC 기준, 포맷: HHmmss)
     */
    @JsonProperty("trade_time")
    @SerializedName("trade_time")
    private String tradeTime;

    /**
     * 최근 거래 일자 (KST 기준, 포맷: yyyyMMdd)
     */
    @JsonProperty("trade_date_kst")
    @SerializedName("trade_date_kst")
    private String tradeDateKst;

    /**
     * 최근 거래 시각 (KST 기준, 포맷: HHmmss)
     */
    @JsonProperty("trade_time_kst")
    @SerializedName("trade_time_kst")
    private String tradeTimeKst;

    /**
     * 최근 거래 일시 (Unix Timestamp)
     */
    @JsonProperty("trade_timestamp")
    @SerializedName("trade_timestamp")
    private Long tradeTimestamp;

    /**
     * 시가 (Opening Price)
     */
    @JsonProperty("opening_price")
    @SerializedName("opening_price")
    private Double openingPrice;

    /**
     * 고가 (High Price)
     */
    @JsonProperty("high_price")
    @SerializedName("high_price")
    private Double highPrice;

    /**
     * 저가 (Low Price)
     */
    @JsonProperty("low_price")
    @SerializedName("low_price")
    private Double lowPrice;

    /**
     * 현재가 (Trade Price)
     */
    @JsonProperty("trade_price")
    @SerializedName("trade_price")
    private Double tradePrice;

    /**
     * 전일 종가 (Previous Closing Price)
     */
    @JsonProperty("prev_closing_price")
    @SerializedName("prev_closing_price")
    private Double prevClosingPrice;

    /**
     * 보합/상승/하락 상태
     * - EVEN: 보합
     * - RISE: 상승
     * - FALL: 하락
     */
    @JsonProperty("change")
    @SerializedName("change")
    private String change;

    /**
     * 변화액 (절대값)
     */
    @JsonProperty("change_price")
    @SerializedName("change_price")
    private Double changePrice;

    /**
     * 변화율 (절대값)
     */
    @JsonProperty("change_rate")
    @SerializedName("change_rate")
    private Double changeRate;

    /**
     * 부호가 있는 변화액 (상승: +, 하락: -)
     */
    @JsonProperty("signed_change_price")
    @SerializedName("signed_change_price")
    private Double signedChangePrice;

    /**
     * 부호가 있는 변화율 (상승: +, 하락: -)
     */
    @JsonProperty("signed_change_rate")
    @SerializedName("signed_change_rate")
    private Double signedChangeRate;

    /**
     * 가장 최근 거래량
     */
    @JsonProperty("trade_volume")
    @SerializedName("trade_volume")
    private Double tradeVolume;

    /**
     * 누적 거래대금 (UTC 0시 기준)
     */
    @JsonProperty("acc_trade_price")
    @SerializedName("acc_trade_price")
    private Double accTradePrice;

    /**
     * 24시간 누적 거래대금
     */
    @JsonProperty("acc_trade_price_24h")
    @SerializedName("acc_trade_price_24h")
    private Double accTradePrice24h;

    /**
     * 누적 거래량 (UTC 0시 기준)
     */
    @JsonProperty("acc_trade_volume")
    @SerializedName("acc_trade_volume")
    private Double accTradeVolume;

    /**
     * 24시간 누적 거래량
     */
    @JsonProperty("acc_trade_volume_24h")
    @SerializedName("acc_trade_volume_24h")
    private Double accTradeVolume24h;

    /**
     * 52주 신고가
     */
    @JsonProperty("highest_52_week_price")
    @SerializedName("highest_52_week_price")
    private Double highest52WeekPrice;

    /**
     * 52주 신고가 달성일 (포맷: yyyy-MM-dd)
     */
    @JsonProperty("highest_52_week_date")
    @SerializedName("highest_52_week_date")
    private String highest52WeekDate;

    /**
     * 52주 신저가
     */
    @JsonProperty("lowest_52_week_price")
    @SerializedName("lowest_52_week_price")
    private Double lowest52WeekPrice;

    /**
     * 52주 신저가 달성일 (포맷: yyyy-MM-dd)
     */
    @JsonProperty("lowest_52_week_date")
    @SerializedName("lowest_52_week_date")
    private String lowest52WeekDate;

    /**
     * 타임스탬프 (응답 생성 시각)
     */
    @JsonProperty("timestamp")
    @SerializedName("timestamp")
    private Long timestamp;

}