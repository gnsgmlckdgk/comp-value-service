package com.finance.dart.cointrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 코인 자동매매 보유 종목 조회 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CointradeHoldingDto {

    private String coinCode;
    private BigDecimal buyPrice;
    private BigDecimal quantity;
    private BigDecimal totalAmount;
    private BigDecimal peakPrice;
    private BigDecimal momentumScore;
    private BigDecimal mlConfidence;
    private String entryReason;
    private Long scannerSignalId;
    private BigDecimal takeProfitPrice;
    private BigDecimal stopLossPrice;
    private LocalDateTime maxHoldUntil;
    private LocalDateTime buyDate;
}
