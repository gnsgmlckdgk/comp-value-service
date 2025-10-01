package com.finance.dart.api.abroad.dto.fmp.incomestatement;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import lombok.*;

/**
 * FMP 영업이익 조회 요청 DTO
 */

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IncomeStatReqDto extends FmpReqCommon {

    /** 검색명 (필수) **/
    private String symbol;

    /** 최근 몇개까지 조회할지 개수 **/
    private Integer limit;

    /**
     * <pre>
     * 기간
     * {@link com.finance.dart.api.abroad.consts.FmpPeriod}
     * </pre>
     */
    private String period;

}
