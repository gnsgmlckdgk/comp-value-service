package com.finance.dart.cointrade.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 코인 자동매매 설정값 수정 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CointradeConfigUpdateDto {

    private String configKey;
    private String configValue;
}