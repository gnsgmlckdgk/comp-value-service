package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 백테스트 옵티마이저 결과 데이터 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizerResultDataDto {

    @JsonProperty("best_params")
    private Map<String, Double> bestParams;

    @JsonProperty("best_score")
    private Double bestScore;

    @JsonProperty("best_return")
    private Double bestReturn;

    @JsonProperty("total_trials")
    private Integer totalTrials;

    @JsonProperty("completed_trials")
    private Integer completedTrials;

    @JsonProperty("failed_trials")
    private Integer failedTrials;

    @JsonProperty("elapsed_time_seconds")
    private Double elapsedTimeSeconds;

    @JsonProperty("stop_reason")
    private String stopReason;

    @JsonProperty("fixed_params")
    private Map<String, Object> fixedParams;

    @JsonProperty("param_ranges")
    private Map<String, OptimizerParamRangeResultDto> paramRanges;

    @JsonProperty("all_trials")
    private List<OptimizerTrialDto> allTrials;
}
