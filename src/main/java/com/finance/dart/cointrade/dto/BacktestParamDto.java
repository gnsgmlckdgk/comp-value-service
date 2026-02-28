package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BacktestParamDto {

    @JsonProperty("initial_capital")
    private Integer initialCapital;

    @JsonProperty("buy_amount_per_coin")
    private Double buyAmountPerCoin;

    @JsonProperty("min_up_probability")
    private Double minUpProbability;

    @JsonProperty("buy_profit_threshold")
    private Double buyProfitThreshold;

    @JsonProperty("take_profit_buffer")
    private Double takeProfitBuffer;

    @JsonProperty("stop_loss_threshold")
    private Double stopLossThreshold;

    @JsonProperty("min_profit_rate")
    private Double minProfitRate;

    @JsonProperty("max_profit_rate")
    private Double maxProfitRate;

    @JsonProperty("prediction_days")
    private Integer predictionDays;

    @JsonProperty("max_holding_days")
    private Integer maxHoldingDays;

    @JsonProperty("sequence_length")
    private Integer sequenceLength;

    @JsonProperty("ensemble_mode")
    private String ensembleMode;

    @JsonProperty("buy_fee_rate")
    private String buyFeeRate;

    @JsonProperty("sell_fee_rate")
    private String selleeRate;

    @JsonProperty("btc_filter_enabled")
    private Boolean btcFilterEnabled;

    @JsonProperty("btc_trend_ma_period")
    private Integer btcTrendMaPeriod;

    @JsonProperty("trailing_stop_enabled")
    private Boolean trailingStopEnabled;

    @JsonProperty("trailing_stop_rate")
    private Double trailingStopRate;

    @JsonProperty("trailing_stop_activation")
    private Double trailingStopActivation;

    @JsonProperty("min_model_agreement")
    private Double minModelAgreement;
}
