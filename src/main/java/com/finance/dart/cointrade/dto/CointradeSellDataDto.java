package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 코인 매도 응답 데이터 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CointradeSellDataDto {

    @JsonProperty("total")
    private Integer total;

    @JsonProperty("success")
    private Integer success;

    @JsonProperty("failed")
    private Integer failed;

    @JsonProperty("results")
    private List<CointradeSellResultDto> results;
}
