package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 코인 매도 개별 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CointradeSellResultDto {

    @JsonProperty("coin_code")
    private String coinCode;

    @JsonProperty("success")
    private Boolean success;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("buy_price")
    private BigDecimal buyPrice;

    @JsonProperty("sell_price")
    private BigDecimal sellPrice;

    @JsonProperty("profit_loss")
    private BigDecimal profitLoss;

    @JsonProperty("profit_loss_rate")
    private BigDecimal profitLossRate;

    @JsonProperty("sell_total")
    private BigDecimal sellTotal;

    @JsonProperty("reason")
    private String reason;
}
