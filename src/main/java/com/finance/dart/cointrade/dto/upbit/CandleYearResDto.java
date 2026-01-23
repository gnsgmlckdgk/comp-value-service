package com.finance.dart.cointrade.dto.upbit;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 연(Year) 캔들 응답 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CandleYearResDto extends CandleCommonResDto {

    /**
     * 캔들 기간의 가장 첫 날
     */
    private String first_day_of_period;
}
