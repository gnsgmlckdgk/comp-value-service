package com.finance.dart.api.abroad.dto.fmp.forexquote;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ForexQuoteReqDto extends FmpReqCommon {

    /** 검색명 (필수) **/
    private String symbol;

}
