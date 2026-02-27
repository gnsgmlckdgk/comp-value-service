package com.finance.dart.cointrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 코인 자동매매 거래 기록 조회 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CointradeTradeHistoryDto {

    private Long id;
    private String coinCode;
    private String tradeType;
    private BigDecimal price;
    private BigDecimal quantity;
    private BigDecimal totalAmount;
    private String reason;  // SIGNAL/TAKE_PROFIT/STOP_LOSS/EXPIRED
    private BigDecimal profitLoss;
    private BigDecimal profitLossRate;
    private BigDecimal predictedHigh;
    private BigDecimal predictedLow;
    private BigDecimal upProbability;
    private BigDecimal downProbability;
    private BigDecimal expectedReturn;
    private LocalDateTime createdAt;
}
