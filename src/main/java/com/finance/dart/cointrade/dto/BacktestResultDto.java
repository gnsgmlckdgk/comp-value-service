package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 백테스트 결과 조회 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestResultDto {

    private String status;

    private BacktestResultDataDto data;

    // running 상태일 때 사용되는 필드들
    private String message;

    private String progress;

    @JsonProperty("current_step")
    private Integer currentStep;

    @JsonProperty("total_steps")
    private Integer totalSteps;

    private BacktestParamDto params;
}
