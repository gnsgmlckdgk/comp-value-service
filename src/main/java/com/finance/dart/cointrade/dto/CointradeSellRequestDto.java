package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 코인 매도 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CointradeSellRequestDto {

    @JsonProperty("coin_codes")
    private List<String> coinCodes;
}
