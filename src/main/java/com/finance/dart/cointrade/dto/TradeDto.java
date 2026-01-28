package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 백테스트 거래 정보 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeDto {

    @JsonProperty("entry_date")
    private String entryDate;

    @JsonProperty("entry_price")
    private Double entryPrice;

    @JsonProperty("exit_date")
    private String exitDate;

    @JsonProperty("exit_price")
    private Double exitPrice;

    private Double quantity;

    @JsonProperty("profit_loss")
    private Double profitLoss;

    @JsonProperty("profit_loss_rate")
    private Double profitLossRate;

    private String reason;

    @JsonProperty("predicted_high")
    private Double predictedHigh;

    @JsonProperty("predicted_low")
    private Double predictedLow;

    @JsonProperty("up_probability")
    private Double upProbability;

    @JsonProperty("expected_return")
    private Double expectedReturn;
}
