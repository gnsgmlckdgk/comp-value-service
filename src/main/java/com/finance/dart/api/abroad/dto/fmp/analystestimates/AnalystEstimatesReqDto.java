package com.finance.dart.api.abroad.dto.fmp.analystestimates;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AnalystEstimatesReqDto extends FmpReqCommon {

    /** 검색명 (필수) **/
    private String symbol;

    /** 조회 기간 (annual/quarter) **/
    private String period;

    /** 최근 몇개까지 조회할지 개수 **/
    private Integer limit;

}
