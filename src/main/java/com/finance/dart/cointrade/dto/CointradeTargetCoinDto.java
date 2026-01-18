package com.finance.dart.cointrade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 코인 자동매매 대상 종목 조회 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CointradeTargetCoinDto {

    private String coinCode;
    private String coinName;
    private Boolean isActive;
}
