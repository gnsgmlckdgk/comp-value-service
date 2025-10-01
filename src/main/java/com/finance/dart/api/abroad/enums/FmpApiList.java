package com.finance.dart.api.abroad.enums;


import org.springframework.http.HttpMethod;

public enum FmpApiList {

    /** 기업명으로 거래소심볼 조회 **/
    CompanyStockSymbolSearch(
            "Company Name Search API",
            "https://financialmodelingprep.com/stable/search-name",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs/stable/search-name",
            "기업명으로 거래소심볼 조회 API"
    ),

    /** 기업 프로파일 조회 **/
    CompanyProfileData(
            "Company Profile Data API",
            "https://financialmodelingprep.com/stable/profile",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs/stable/profile-symbol",
            "기업 프로파일 조회 API"
    ),

    /** 영업이익 조회 **/
    IncomeStatement(
            "Income Statement Data API",
            "https://financialmodelingprep.com/stable/income-statement",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs#income-statement",
            "영업이익 조회 API"
    ),

    /** 재무상태표 조회 **/
    BalanceSheetStatement(
            "Balance Sheet Statement Data API",
            "https://financialmodelingprep.com/stable/balance-sheet-statement",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs#balance-sheet-statement",
            "재무상태표 조회 API"
    ),

    /** 주요 재무지표 조회 **/
    KeyMetrics(
            "Key Metrics Data API",
            "https://financialmodelingprep.com/stable/key-metrics",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs#key-metrics",
            "주요 재무지표 조회 API"
    ),



    ;

    public final String name;
    public final String url;
    public final HttpMethod method;
    public final String apiDoc;
    public final String desc;


    FmpApiList(String name, String url, HttpMethod method, String apiDoc, String desc) {
        this.name = name;
        this.url = url;
        this.method = method;
        this.apiDoc = apiDoc;
        this.desc = desc;
    }

}
