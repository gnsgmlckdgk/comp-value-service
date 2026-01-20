package com.finance.dart.cointrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 코인 자동매매 현재 상태 조회 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CointradeStatusDto {

    private Boolean buySchedulerEnabled;
    private Boolean sellSchedulerEnabled;
    private String buyCheckHours;
    private CointradeNextRunDto buyNextRun;
    private String sellCheckSeconds;
    private String priceMonitorSeconds;
    private Integer holdingCount;
    private Integer totalBuyCount;
    private Integer totalSellCount;
    private BigDecimal totalProfitLoss;
    private BigDecimal totalProfitLossRate;
}
