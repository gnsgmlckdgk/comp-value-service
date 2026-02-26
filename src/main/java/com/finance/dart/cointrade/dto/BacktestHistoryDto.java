package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 백테스트 이력 조회 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestHistoryDto {

    @JsonProperty("task_id")
    private String taskId;

    private String title;

    private String status;

    @JsonProperty("coin_codes")
    private List<String> coinCodes;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("total_return")
    private Double totalReturn;

    @JsonProperty("win_rate")
    private Double winRate;

    @JsonProperty("total_trades")
    private Integer totalTrades;

    @JsonProperty("max_drawdown")
    private Double maxDrawdown;

    @JsonProperty("sharpe_ratio")
    private Double sharpeRatio;

    @JsonProperty("coin_count")
    private Integer coinCount;

    @JsonProperty("invested_return")
    private Double investedReturn;

    @JsonProperty("created_at")
    private String createdAt;
}
