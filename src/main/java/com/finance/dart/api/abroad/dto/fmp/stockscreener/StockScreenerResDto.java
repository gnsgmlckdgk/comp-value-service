package com.finance.dart.api.abroad.dto.fmp.stockscreener;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class StockScreenerResDto {

    /** 종목 티커 (예: BAC-PL) */
    private String symbol;

    /** 회사명 */
    private String companyName;

    /** 시가총액 */
    private Long marketCap;

    /** 섹터 (예: Financial Services) */
    private String sector;

    /** 산업군 (예: Banks—Diversified) */
    private String industry;

    /** 베타값 */
    private Double beta;

    /** 현재 주가 */
    private Double price;

    /** 마지막 연간 배당 (last annual dividend) */
    private Double lastAnnualDividend;

    /** 거래량 */
    private Long volume;

    /** 거래소 전체 이름 (예: New York Stock Exchange) */
    private String exchange;

    /** 거래소 단축 이름 (예: NYSE) */
    private String exchangeShortName;

    /** 국가 코드 (예: US) */
    private String country;

    /** ETF 여부 */
    private Boolean isEtf;

    /** 펀드 여부 */
    private Boolean isFund;

    /** 활발히 거래되는 종목 여부 */
    private Boolean isActivelyTrading;
}
