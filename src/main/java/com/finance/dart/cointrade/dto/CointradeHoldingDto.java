package com.finance.dart.cointrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private BigDecimal predictedHigh;
    private BigDecimal predictedLow;
    private LocalDateTime buyDate;
    private BigDecimal surgeProbability;
    private LocalDate surgeDay;
    private LocalDate expireDate;
    private BigDecimal buyScore;
}
