package com.finance.dart.api.common.dto;

import lombok.Data;

/**
 * 1주당 가치 계산 결과
 */
@Data
public class CompanySharePriceResultDetail {

    private String 단위 = "";

    private String 영업이익_전전기 = "";
    private String 영업이익_전기 = "";
    private String 영업이익_당기 = "";
    private String 영업이익_합계 = "";
    private String 영업이익_평균 = "";

    private String 유동자산합계 = "";

    private String 유동부채합계 = "";

    private String 유동비율 = "";

    private String 투자자산_비유동자산내 = "";

    private String 고정부채 = "";

    private String 발행주식수 = "";

    /** 계산값 **/
    private String 계산_사업가치 = "";
    private String 계산_재산가치 = "";
    private String 계산_부채 = "";
    private String 계산_기업가치 = "";
    private String PER = "";

    /** 기타값 **/
    private String 예외메세지_영업이익 = "";
    private String 예외메시지_발행주식수 = "";

    public CompanySharePriceResultDetail(String unit) {
        this.단위 = unit;
    }

}
