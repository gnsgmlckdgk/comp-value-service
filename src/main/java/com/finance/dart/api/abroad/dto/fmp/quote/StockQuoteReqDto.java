package com.finance.dart.api.abroad.dto.fmp.quote;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StockQuoteReqDto extends FmpReqCommon {

    /** 검색명 (필수) **/
    private String symbol;

}
