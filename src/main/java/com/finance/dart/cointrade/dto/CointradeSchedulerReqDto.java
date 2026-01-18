package com.finance.dart.cointrade.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 코인 자동매매 스케줄러 ON/OFF 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CointradeSchedulerReqDto {

    private Boolean enabled;
}
