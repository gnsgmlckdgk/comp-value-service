package com.finance.dart.api.abroad.dto.fmp.keymetrics;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import lombok.*;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class KeyMetricsReqDto extends FmpReqCommon {

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
