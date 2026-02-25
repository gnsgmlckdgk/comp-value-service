package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 백테스트 옵티마이저 이력 조회 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizerHistoryDto {

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

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("completed_at")
    private String completedAt;

    @JsonProperty("best_score")
    private Double bestScore;

    @JsonProperty("best_return")
    private Double bestReturn;

    @JsonProperty("total_trials")
    private Integer totalTrials;

    @JsonProperty("stop_reason")
    private String stopReason;
}
