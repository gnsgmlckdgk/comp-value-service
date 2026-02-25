package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 백테스트 옵티마이저 실행 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizerRunReqDto {

    @JsonProperty("coin_codes")
    private List<String> coinCodes;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    private String title;

    @JsonProperty("max_runs")
    private Integer maxRuns;

    @JsonProperty("max_time_minutes")
    private Integer maxTimeMinutes;

    @JsonProperty("target_return")
    private Double targetReturn;

    @JsonProperty("num_workers")
    private Integer numWorkers;

    @JsonProperty("custom_param_ranges")
    private Map<String, OptimizerParamRangeDto> customParamRanges;
}
