package com.finance.dart.cointrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 코인 자동매매 설정값 조회 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CointradeConfigDto {

    private String paramName;
    private String paramValue;
    private String description;
    private LocalDateTime updatedAt;
}
