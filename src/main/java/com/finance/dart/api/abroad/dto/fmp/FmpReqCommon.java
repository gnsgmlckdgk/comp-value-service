package com.finance.dart.api.abroad.dto.fmp;

import lombok.Getter;
import lombok.Setter;


/**
 * FMP 필수 요청 파라미터 DTO
 */
@Getter
@Setter
public class FmpReqCommon {
    /**
     * API KEY
     */
    private String apikey;

    public FmpReqCommon(String apiKey) {
        this.apikey = apiKey;
    }
}
