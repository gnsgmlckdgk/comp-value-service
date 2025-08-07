package com.finance.dart.api.abroad.dto.company;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finance.dart.api.abroad.dto.common.RequireParamDto;
import lombok.Getter;
import lombok.ToString;

/**
 * CompanyStockSymbolSearch API 요청
 */

@Getter
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FindCompanySymbolReqDto extends RequireParamDto {

    /** 검색명 (필수) **/
    private String query;

    /** 조회최대개수 **/
    private String limit;

    /** 거래소 **/
    private String exchange;


    private FindCompanySymbolReqDto(String apiKey, String query, String limit, String exchange) {
        super(apiKey);
        this.query = query;
        this.limit = limit;
        this.exchange = exchange;
    }

    /**
     * FindCompanySymbolDto 생성
     * @param apiKey
     * @param query
     * @param limit
     * @param exchange
     * @return
     */
    public static FindCompanySymbolReqDto of(String apiKey, String query, String limit, String exchange) {
        return new FindCompanySymbolReqDto(apiKey, query, limit, exchange);
    }

    /**
     * FindCompanySymbolDto 생성
     * @param apiKey
     * @param query
     * @return
     */
    public static FindCompanySymbolReqDto ofQuery(String apiKey, String query) {
        return new FindCompanySymbolReqDto(apiKey, query, null, null);
    }

}
