package com.finance.dart.api.abroad.enums;


import org.springframework.http.HttpMethod;

/**
 * SEC API 목록
 */
public enum SecApiList {

    /* 테스트 CIK : 0000789019 (MICROSOFT CORPORATION) */

    /** 영업이익 조회 **/
    OperatingIncomeLoss(
            "XBRL 재무제표항목 OperatingIncomeLoss API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK{cik}/us-gaap/OperatingIncomeLoss.json",
            HttpMethod.GET,
            "https://www.sec.gov/search-filings/edgar-application-programming-interfaces",
            "XBRL 영업이익 조회 API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK0000789019/us-gaap/OperatingIncomeLoss.json"
    ),

    /** 유동자산 합계 조회 **/
    AssetsCurrent(
            "XBRL 재무제표항목 AssetsCurrent API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK{cik}/us-gaap/AssetsCurrent.json",
            HttpMethod.GET,
            "https://www.sec.gov/search-filings/edgar-application-programming-interfaces",
            "XBRL 유동자산 합계 조회 API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK0000789019/us-gaap/AssetsCurrent.json"
    ),

    /** 유동부채 합계 조회 **/
    LiabilitiesCurrent(
            "XBRL 재무제표항목 LiabilitiesCurrent API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK{cik}/us-gaap/LiabilitiesCurrent.json",
            HttpMethod.GET,
            "https://www.sec.gov/search-filings/edgar-application-programming-interfaces",
            "XBRL 유동부채 합계 조회 API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK0000789019/us-gaap/LiabilitiesCurrent.json"
    ),

    /** ----------------------- 비유동자산내 투자자산 조회(4가지 조회, 없는 항목이 있을 수 있음) ----------------------- **/

    /** 1. 장기매도 가능 증권 AvailableForSaleSecuritiesNoncurrent **/
    AvailableForSaleSecuritiesNoncurrent(
            "XBRL 재무제표항목 AvailableForSaleSecuritiesNoncurrent API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK{cik}/us-gaap/AvailableForSaleSecuritiesNoncurrent.json",
            HttpMethod.GET,
            "https://www.sec.gov/search-filings/edgar-application-programming-interfaces",
            "XBRL 장기매도 가능 증권 조회 API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK0000789019/us-gaap/AvailableForSaleSecuritiesNoncurrent.json"
    ),

    /** 2. 지분법 투자 EquityMethodInvestments **/
    LongTermInvestments(
            "XBRL 지분법 투자 EquityMethodInvestments API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK{cik}/us-gaap/EquityMethodInvestments.json",
            HttpMethod.GET,
            "https://www.sec.gov/search-filings/edgar-application-programming-interfaces",
            "XBRL 지분법 투자 조회 API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK0000789019/us-gaap/EquityMethodInvestments.json"
    ),

    /** 3. 기타 장기투자 OtherInvestments**/
    OtherInvestments(
            "XBRL 재무제표항목 OtherInvestments API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK{cik}/us-gaap/OtherInvestments.json",
            HttpMethod.GET,
            "https://www.sec.gov/search-filings/edgar-application-programming-interfaces",
            "XBRL 장기투자 조회 API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK0000789019/us-gaap/OtherInvestments.json"
    ),

    /** 4. 투자 및 대여금 InvestmentsAndAdvances**/
    InvestmentsAndAdvances(
            "XBRL 재무제표항목 InvestmentsAndAdvances API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK{cik}/us-gaap/InvestmentsAndAdvances.json",
            HttpMethod.GET,
            "https://www.sec.gov/search-filings/edgar-application-programming-interfaces",
            "XBRL 투자 및 대여금 조회 API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK0000789019/us-gaap/InvestmentsAndAdvances.json"
    ),

    /** ---------------------------------------------- **/

    /** 총 부채 조회 **/
    Liabilities(
            "XBRL 재무제표항목 Liabilities API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK{cik}/us-gaap/Liabilities.json",
            HttpMethod.GET,
            "https://www.sec.gov/search-filings/edgar-application-programming-interfaces",
            "XBRL 총부채 조회 API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK0000789019/us-gaap/Liabilities.json"
    ),

    /** 고정부채 합계 조회 **/
    LiabilitiesNoncurrent(
            "XBRL 재무제표항목 LiabilitiesNoncurrent API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK{cik}/us-gaap/LiabilitiesNoncurrent.json",
            HttpMethod.GET,
            "https://www.sec.gov/search-filings/edgar-application-programming-interfaces",
            "XBRL 고정부채 조회 API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK0000789019/us-gaap/LiabilitiesNoncurrent.json"
    ),

    /** 발행주식수 조회 **/
    EntityCommonStockSharesOutstanding(
            "XBRL 기업메타데이터 EntityCommonStockSharesOutstanding API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK{cik}/dei/EntityCommonStockSharesOutstanding.json",
            HttpMethod.GET,
            "https://www.sec.gov/search-filings/edgar-application-programming-interfaces",
            "XBRL 발행주식수 조회 API",
            "https://data.sec.gov/api/xbrl/companyconcept/CIK0000789019/dei/EntityCommonStockSharesOutstanding.json"
    ),
    ;

    public final String name;
    public final String url;
    public final HttpMethod method;
    public final String apiDoc;
    public final String desc;
    public final String sample;


    SecApiList(String name, String url, HttpMethod method, String apiDoc, String desc) {
        this.name = name;
        this.url = url;
        this.method = method;
        this.apiDoc = apiDoc;
        this.desc = desc;
        this.sample = "";
    }

    SecApiList(String name, String url, HttpMethod method, String apiDoc, String desc, String sample) {
        this.name = name;
        this.url = url;
        this.method = method;
        this.apiDoc = apiDoc;
        this.desc = desc;
        this.sample = sample;
    }

}
