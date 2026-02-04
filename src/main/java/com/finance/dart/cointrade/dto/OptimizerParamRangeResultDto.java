package com.finance.dart.cointrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 옵티마이저 결과 파라미터 범위 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptimizerParamRangeResultDto {

    private Double min;

    private Double max;

    private Double step;
}
