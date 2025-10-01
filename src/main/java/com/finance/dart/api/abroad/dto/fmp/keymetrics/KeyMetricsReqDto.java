package com.finance.dart.api.abroad.dto.fmp.keymetrics;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class KeyMetricsReqDto extends FmpReqCommon {

    /** 검색명 (필수) **/
    private String symbol;

    /** 최근 몇개까지 조회할지 개수 **/
    private int limit;

    /**
     * <pre>
     * 기간
     * {@link com.finance.dart.api.abroad.consts.FmpPeriod}
     * </pre>
     */
    private String period;

    private KeyMetricsReqDto(String apiKey, String symbol) {
        super(apiKey);
        this.symbol = symbol;
    }

    /**
     * KeyMetricsReqDto create
     * @param apiKey
     * @param symbol
     * @return
     */
    public KeyMetricsReqDto of(String apiKey, String symbol) {
        return new KeyMetricsReqDto(apiKey, symbol);
    }

}
