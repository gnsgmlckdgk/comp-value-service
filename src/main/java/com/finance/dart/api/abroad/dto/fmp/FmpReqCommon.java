package com.finance.dart.api.abroad.dto.fmp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


/**
 * FMP 필수 요청 파라미터 DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class FmpReqCommon {
    /**
     * API KEY
     */
    private String apikey;

    public FmpReqCommon(String apiKey) {
        this.apikey = apiKey;
    }
}
