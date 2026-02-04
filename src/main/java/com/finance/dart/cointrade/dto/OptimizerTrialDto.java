package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 백테스트 옵티마이저 개별 시행 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizerTrialDto {

    @JsonProperty("trial_id")
    private Integer trialId;

    private Map<String, Double> params;

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

    @JsonProperty("stability_score")
    private Double stabilityScore;

    @JsonProperty("final_score")
    private Double finalScore;
}
