package com.finance.dart.cointrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 코인 자동매매 스캐너 시그널 조회 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CointradeScannerSignalDto {

    private Long id;
    private String coinCode;
    private LocalDateTime detectedAt;
    private String signalType;
    private BigDecimal momentumScore;
    private BigDecimal volumeRatio;
    private BigDecimal priceChangePct;
    private BigDecimal rsiValue;
    private BigDecimal vwapDeviation;
    private BigDecimal mlConfidence;
    private String actionTaken;
    private BigDecimal currentPrice;
    private LocalDateTime createdAt;
}
