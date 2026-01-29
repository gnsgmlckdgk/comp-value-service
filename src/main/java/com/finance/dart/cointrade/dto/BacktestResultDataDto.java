package com.finance.dart.cointrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 백테스트 결과 데이터 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestResultDataDto {

    private PortfolioDto portfolio;

    private BacktestParamDto params;

    private Map<String, IndividualResultDto> individual;
}
