package com.finance.dart.api.abroad.dto.fmp.stocknews;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StockNewsReqDto extends FmpReqCommon {

    /** 종목 심볼 (예: AAPL) */
    private String tickers;

    /** 조회 건수 */
    private Integer limit;
}
