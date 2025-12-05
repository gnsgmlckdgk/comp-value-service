package com.finance.dart.api.abroad.dto.fmp.chart;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import lombok.*;

/**
 * 주식 가격 및 거래량 데이터 요청 DTO
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StockPriceVolumeReqDto extends FmpReqCommon {

    /** 종목 심볼 (필수) - 예: AAPL */
    private String symbol;

    /** 시작일(yyyy-MM-dd) **/
    private String from;

    /** 종료일(yyyy-MM-dd) **/
    private String to;

}
