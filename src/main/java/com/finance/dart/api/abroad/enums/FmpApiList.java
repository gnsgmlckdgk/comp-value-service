package com.finance.dart.api.abroad.enums;


import org.springframework.http.HttpMethod;

public enum FmpApiList {

    /** ========== 거래소 및 기업 조회 ========== **/

    /** Symbol로 거래소심볼 조회 **/
    CompanyStockSymbolSearchBySymbol(
            "Stock Symbol Search API",
            "https://financialmodelingprep.com/stable/search-symbol",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs/stable/search-symbol",
            "심볼로 거래소심볼 조회 API"
    ),

    /** 기업명으로 거래소심볼 조회 **/
    CompanyStockSymbolSearchByCompanyName(
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


    /** ========== 재무제표 조회 ========== **/

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

    /** 기업가치 조회 **/
    EnterpriseValues(
            "Enterprise Values Data API",
            "https://financialmodelingprep.com/stable/enterprise-values",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs#enterprise-values",
            "기업가치 조회 API"
    ),

    /** 재무비율지표 조회 **/
    FinancialRatios(
            "Financial Ratios Data API",
            "https://financialmodelingprep.com/stable/ratios",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs#ratios",
            "재무비율지표 조회 API"
    ),

    /** 재무비율지표(TTM) 조회 **/
    FinancialRatiosTTM(
            "Financial Ratios Data API",
            "https://financialmodelingprep.com/stable/ratios-ttm",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs#ratios-ttm",
            "재무비율지표(TTM) 조회 API"
    ),

    /** 영업이익 성장률 조회 **/
    IncomeStatementGrowth(
            "Income Statement Growth Data API",
            "https://financialmodelingprep.com/stable/income-statement-growth",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs#income-statement-growth",
            "영업이익 성장률 조회 API"
    ),

    /** 성장률 조회 **/
    FinancialStatementsGrowth(
            "Financial Statement Growth API",
            "https://financialmodelingprep.com/stable/financial-growth",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs#financial-growth",
            "성장률 조회 API"
    ),


    /** ========== 주식관련 데이터 조회 ========== **/

    /** 외환 시세 조회(환율정보 조회) **/
    ForexQuote(
            "Forex Quote API",
            "https://financialmodelingprep.com/stable/quote",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs#quote",
            "외환 시세 조회 API"
    ),

    /** 주식 시세 조회(간소화) **/
    StockQuoteShort(
            "Stock Quote Short API",
            "https://financialmodelingprep.com/stable/quote-short",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs#quote-short",
            "주식 시세 조회 API(간소화 버전)"
    ),

    /** 애프터마켓 시세 조회 **/
    AftermarketTrade(
            "Aftermarket Trade API",
            "https://financialmodelingprep.com/stable/aftermarket-trade",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs#aftermarket-trade",
            "애프터마켓 시세 조회 API"
    ),


    // -- 차트 조회

    /** 기업별 차트 조회 **/
    StockPriceandVolumeData(
            "Stock Price and Volume Data API",
            "https://financialmodelingprep.com/stable/historical-price-eod/full",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs/stable/historical-price-eod-full",
            "주식 가격 그리고 거래량 데이터 조회 API"
    ),

    /** 거래소별 차트 조회 **/
    HistoricalIndexFullChart(
            "Historical Index Full Chart API",
            "https://financialmodelingprep.com/stable/historical-price-eod/full",
            HttpMethod.GET,
            "https://site.financialmodelingprep.com/developer/docs/stable/index-historical-price-eod-full",
            "기록 인덱스 전체 차트 조회 API"
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
