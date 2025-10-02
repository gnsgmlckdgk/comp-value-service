package com.finance.dart.api.abroad.dto.fmp.financialratios;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRatiosTTM_ReqDto extends FmpReqCommon {

    /** 검색명 (필수) **/
    private String symbol;

}
