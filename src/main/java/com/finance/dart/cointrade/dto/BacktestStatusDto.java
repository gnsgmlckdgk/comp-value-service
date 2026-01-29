package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 백테스트 작업 상태 조회 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestStatusDto {

    @JsonProperty("task_id")
    private String taskId;

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

    @JsonProperty("error_message")
    private String errorMessage;

    private String progress;

    private BacktestParamDto params;
}
