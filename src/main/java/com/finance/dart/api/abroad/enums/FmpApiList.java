package com.finance.dart.api.abroad.enums;


import org.springframework.http.HttpMethod;

public enum FmpApiList {


    CompanyNameSearch(
            "Company Name Search API",
            "https://financialmodelingprep.com/stable/search-name",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs/stable/search-name",
            "기업명 조회 API"),


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
