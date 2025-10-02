package com.finance.dart.api.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 1주당 가치 계산 결과
 */
@Data
@NoArgsConstructor
public class CompanySharePriceResult {

    private boolean 정상처리여부 = true;
    private String 결과메시지 = "";

    private String 기업코드 = "";
    private String 기업심볼 = "";
    private String 기업명 = "";

    private String 주식코드 = "";

    private String 주당가치 = "";    // 1주당 가치
    private String 현재가격 = "";    // 현재주식가격
    private String 확인시간 = "";    // yyyyMMdd HH:mm:ss

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private CompanySharePriceResultDetail 상세정보;

    public CompanySharePriceResult(String responseMessage) {
        this.결과메시지 = responseMessage;
    }
}
