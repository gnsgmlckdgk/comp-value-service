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

    @JsonProperty("momentum_score")
    private Double momentumScore;

    @JsonProperty("ml_confidence")
    private Double mlConfidence;

    @JsonProperty("entry_reason")
    private String entryReason;

    @JsonProperty("hold_duration_sec")
    private Integer holdDurationSec;
}
