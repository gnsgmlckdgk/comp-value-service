package com.finance.dart.api.abroad.dto.fmp.balancesheet;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class BalanceSheetReqDto extends FmpReqCommon {

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


    private BalanceSheetReqDto(String apiKey, String symbol) {
        super(apiKey);
        this.symbol = symbol;
    }

    /**
     * BalanceSheetReqDto create
     * @param apiKey
     * @param symbol
     * @return
     */
    public BalanceSheetReqDto of(String apiKey, String symbol) {
        return new BalanceSheetReqDto(apiKey, symbol);
    }

}
