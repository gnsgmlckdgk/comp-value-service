package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 옵티마이저 파라미터 범위 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizerParamRangeDto {

    @JsonProperty("min_value")
    private Double minValue;

    @JsonProperty("max_value")
    private Double maxValue;

    private Double step;
}
