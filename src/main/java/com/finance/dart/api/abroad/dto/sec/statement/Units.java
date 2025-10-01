package com.finance.dart.api.abroad.dto.sec.statement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * SEC XBRL API 응답에서 단위별(예: USD) 값을 담는 DTO
 * 예: "units": { "USD": [ { fy, fp, val, ... }, ... ] }
 */
@Getter
@Setter
@ToString
public class Units {

    /**
     * 단위가 "USD"인 값들의 리스트  
     * - 예: 영업이익(OperatingIncomeLoss)을 USD 단위로 연도별, 분기별로 나열한 리스트  
     * - 항목에는 회계연도(fy), 분기(fp), 값(val), 제출일(filed) 등이 포함됨
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("USD")
    private List<USD> usd;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Shares> shares;
}