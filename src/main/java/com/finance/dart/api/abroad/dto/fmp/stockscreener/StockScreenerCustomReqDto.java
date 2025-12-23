package com.finance.dart.api.abroad.dto.fmp.stockscreener;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class StockScreenerCustomReqDto {

    /** 최대 조회 건수 */
    private Integer limit;

    /** 조회 타입 **/
    private int type;

}
