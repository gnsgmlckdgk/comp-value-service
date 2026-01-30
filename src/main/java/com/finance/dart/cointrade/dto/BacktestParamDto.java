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

    @JsonProperty("sequence_length")
    private Integer sequenceLength;

    @JsonProperty("ensemble_mode")
    private String ensembleMode;

    @JsonProperty("buy_fee_rate")
    private String buyFeeRate;

    @JsonProperty("sell_fee_rate")
    private String selleeRate;
}
