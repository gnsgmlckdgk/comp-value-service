package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 백테스트 설정값
 */
@Getter
@Setter
@ToString
public class BacktestRequestConfigDto {

    /** 초기 자본금 (원, 기본값: DB 또는 1,000,000) */
    @JsonProperty("initial_capital")
    @SerializedName("initial_capital")
    private Double initialCapital;

    /** 1회 매수 금액 (원, 기본값: DB 또는 100,000) */
    @JsonProperty("buy_amount_per_coin")
    @SerializedName("buy_amount_per_coin")
    private Double buyAmountPerCoin;

    /** 최소 상승 확률 (기본값: DB 또는 0.6) */
    @JsonProperty("min_up_probability")
    @SerializedName("min_up_probability")
    private Double minUpProbability;

    /** 기대 수익률 하한 (%, 기본값: DB 또는 10.0) */
    @JsonProperty("buy_profit_threshold")
    @SerializedName("buy_profit_threshold")
    private Double buyProfitThreshold;

    /** 익절 버퍼 (%, 기본값: DB 또는 3.0) */
    @JsonProperty("take_profit_buffer")
    @SerializedName("take_profit_buffer")
    private Double takeProfitBuffer;

    /** 손절 임계값 (%, 기본값: DB 또는 5.0) */
    @JsonProperty("stop_loss_threshold")
    @SerializedName("stop_loss_threshold")
    private Double stopLossThreshold;

    /** 최소 기대 수익률 (%, 기본값: DB 또는 5.0) */
    @JsonProperty("min_profit_rate")
    @SerializedName("min_profit_rate")
    private Double minProfitRate;

    /** 최대 기대 수익률 (%, 기본값: DB 또는 30.0) */
    @JsonProperty("max_profit_rate")
    @SerializedName("max_profit_rate")
    private Double maxProfitRate;

    /** 예측 기간 (일, 기본값: DB 또는 3) */
    @JsonProperty("prediction_days")
    @SerializedName("prediction_days")
    private Integer predictionDays;

    /** 최대 보유 기간 (일, 기본값: DB 또는 7) */
    @JsonProperty("max_holding_days")
    @SerializedName("max_holding_days")
    private Integer maxHoldingDays;

    /** 매수 수수료율 (기본값: 0.0005 = 0.05%) */
    @JsonProperty("buy_fee_rate")
    @SerializedName("buy_fee_rate")
    private Double buyFeeRate;

    /** 매도 수수료율 (기본값: 0.0005 = 0.05%) */
    @JsonProperty("sell_fee_rate")
    @SerializedName("sell_fee_rate")
    private Double sellFeeRate;

    /** 시퀀스 길이 (기본값: 60) */
    @JsonProperty("sequence_length")
    @SerializedName("sequence_length")
    private int sequenceLength = 60;

}
