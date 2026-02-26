package com.finance.dart.cointrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 백테스트 제목 수정 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BacktestUpdateTitleResDto {

    private String status;

    private String message;

    private String title;
}
