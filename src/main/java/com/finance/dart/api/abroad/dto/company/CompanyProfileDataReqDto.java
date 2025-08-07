package com.finance.dart.api.abroad.dto.company;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finance.dart.api.abroad.dto.common.RequireParamDto;
import lombok.Getter;
import lombok.ToString;

/**
 * CompanyProfileData API 요청
 */

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CompanyProfileDataReqDto extends RequireParamDto {

    /** 검색명 (필수) **/
    private String symbol;


    private CompanyProfileDataReqDto(String apiKey, String symbol) {
        super(apiKey);
        this.symbol = symbol;
    }

    /**
     * CompanyProfileDataReqDto 생성
     * @param apiKey
     * @param symbol
     * @return
     */
    public static CompanyProfileDataReqDto of(String apiKey, String symbol) {
        return new CompanyProfileDataReqDto(apiKey, symbol);
    }

}
