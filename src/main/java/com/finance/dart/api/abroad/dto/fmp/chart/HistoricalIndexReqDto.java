package com.finance.dart.api.abroad.dto.fmp.chart;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import lombok.*;

/**
 * 거래소 인덱스 차트 데이터 요청 DTO
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalIndexReqDto extends FmpReqCommon {

    /** 인덱스 심볼 (필수) - 예: ^GSPC (S&P 500), ^DJI (Dow Jones), ^IXIC (NASDAQ) */
    private String symbol;

    /** 시작일(yyyy-MM-dd) **/
    private String from;

    /** 종료일(yyyy-MM-dd) **/
    private String to;

}
