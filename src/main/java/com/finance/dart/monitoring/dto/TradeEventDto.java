package com.finance.dart.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeEventDto {

    private Long id;
    private String coinCode;
    private String tradeType;    // BUY, SELL
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal totalAmount;
    private BigDecimal profitLoss;
    private BigDecimal profitLossRate;
    private String reason;
    private LocalDateTime timestamp;
}
