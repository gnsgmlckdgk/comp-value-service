package com.finance.dart.api.abroad.dto.fmp.ratiosttm;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import lombok.*;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RatiosTtmReqDto extends FmpReqCommon {

    /** 검색명 (필수) **/
    private String symbol;

}
