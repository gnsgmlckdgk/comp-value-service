package com.finance.dart.api.abroad.enums;


import org.springframework.http.HttpMethod;

/**
 * SEC API 목록
 */
public enum SecApiList {

    /** 당기 영업이익 조회 **/
    OperatingIncomeLoss(
            "XBRL companyconcept OperatingIncomeLoss API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK{cik}/us-gaap/OperatingIncomeLoss.json",
            HttpMethod.GET,
            "https://www.sec.gov/search-filings/edgar-application-programming-interfaces",
            "XBRL 당기 영업이익 조회 API"
    ),

    /** 유동자산 합계 조회 **/
    AssetsCurrent(
            "XBRL companyconcept AssetsCurrent API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK{cik}/us-gaap/AssetsCurrent.json",
            HttpMethod.GET,
            "https://www.sec.gov/search-filings/edgar-application-programming-interfaces",
            "XBRL 유동자산 합계 조회 API"
    ),
    ;

    public final String name;
    public final String url;
    public final HttpMethod method;
    public final String apiDoc;
    public final String desc;


    SecApiList(String name, String url, HttpMethod method, String apiDoc, String desc) {
        this.name = name;
        this.url = url;
        this.method = method;
        this.apiDoc = apiDoc;
        this.desc = desc;
    }

}
