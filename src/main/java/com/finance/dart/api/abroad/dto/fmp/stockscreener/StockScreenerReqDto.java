package com.finance.dart.api.abroad.dto.fmp.stockscreener;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class StockScreenerReqDto extends FmpReqCommon {

    /** 시가총액 최소값 */
    private Long marketCapMoreThan;

    /** 시가총액 최대값 */
    private Long marketCapLowerThan;

    /** 주가 최소값 */
    private Double priceMoreThan;

    /** 주가 최대값 */
    private Double priceLowerThan;

    /** 베타값 최소값 */
    private Double betaMoreThan;

    /** 베타값 최대값 */
    private Double betaLowerThan;

    /** 거래량 최소값 */
    private Long volumeMoreThan;

    /** 거래량 최대값 */
    private Long volumeLowerThan;

    /** 배당 수익률 최소값 */
    private Double dividendMoreThan;

    /** 배당 수익률 최대값 */
    private Double dividendLowerThan;

    /** ETF 여부 */
    private Boolean isEtf;

    /** 펀드 여부 */
    private Boolean isFund;

    /** 활발히 거래되는 종목 여부 */
    private Boolean isActivelyTrading;

    /** 섹터 (예: Technology) */
    private String sector;

    /** 산업군 (예: Software) */
    private String industry;

    /** 국가 (예: US) */
    private String country;

    /** 거래소 (예: NASDAQ) */
    private String exchange;

    /** 최대 조회 건수 */
    private Integer limit;
}
