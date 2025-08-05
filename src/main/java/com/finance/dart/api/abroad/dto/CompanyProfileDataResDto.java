package com.finance.dart.api.abroad.dto;

import lombok.Data;

/**
 * CompanyProfileData API 응답 DTO
 */
@Data
public class CompanyProfileDataResDto {

    /** 본사 주소 */
    private String address;

    /** 평균 거래량 */
    private Long averageVolume;

    /** 주가 베타 값 (변동성 지표) */
    private Double beta;

    /** 최고경영자(CEO) 이름 */
    private String ceo;

    /** 전일 대비 주가 등락폭 */
    private Double change;

    /** 전일 대비 주가 등락률 (%) */
    private Double changePercentage;

    /** SEC 고유 식별번호 (CIK) */
    private String cik;

    /** 본사 도시 */
    private String city;

    /** 회사 이름 */
    private String companyName;

    /** 본사 국가 코드 */
    private String country;

    /** 통화 코드 (예: USD) */
    private String currency;

    /** CUSIP (미국 고유 종목 식별번호) */
    private String cusip;

    /** 기본 이미지 여부 (UI용) */
    private Boolean defaultImage;

    /** 회사 설명 */
    private String description;

    /** 거래소 코드 (예: NASDAQ) */
    private String exchange;

    /** 거래소 전체 이름 */
    private String exchangeFullName;

    /** 상근 직원 수 */
    private String fullTimeEmployees;

    /** 회사 로고 이미지 URL */
    private String image;

    /** 산업군 (Industry) */
    private String industry;

    /** 상장일 (IPO 날짜) */
    private String ipoDate;

    /** 현재 거래 활성화 여부 */
    private Boolean isActivelyTrading;

    /** ADR(미국예탁증서) 여부 */
    private Boolean isAdr;

    /** ETF 여부 */
    private Boolean isEtf;

    /** 펀드 여부 */
    private Boolean isFund;

    /** ISIN (국제 증권 식별번호) */
    private String isin;

    /** 최근 배당금 (주당 기준) */
    private Double lastDividend;

    /** 시가총액 (단위: USD) */
    private Long marketCap;

    /** 본사 전화번호 */
    private String phone;

    /** 현재 주가 */
    private Double price;

    /** 52주 주가 범위 (최저가 - 최고가) */
    private String range;

    /** 업종 (Sector) */
    private String sector;

    /** 본사 주(State) */
    private String state;

    /** 주식 종목 코드 (심볼, 티커) */
    private String symbol;

    /** 당일 거래량 */
    private Long volume;

    /** 회사 공식 웹사이트 */
    private String website;

    /** 본사 우편번호 */
    private String zip;
}