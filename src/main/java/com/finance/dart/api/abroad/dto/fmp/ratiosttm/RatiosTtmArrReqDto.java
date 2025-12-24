package com.finance.dart.api.abroad.dto.fmp.ratiosttm;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RatiosTtmArrReqDto extends FmpReqCommon {

    /** 검색명 (필수) **/
    private List<String> symbol;

}
