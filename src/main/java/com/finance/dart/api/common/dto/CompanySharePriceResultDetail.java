package com.finance.dart.api.common.dto;

import lombok.Data;

/**
 * 1주당 가치 계산 결과
 */
@Data
public class CompanySharePriceResultDetail {

    private String 단위 = "N/A";

    private String 영업이익_전전기 = "N/A";
    private String 영업이익_전기 = "N/A";
    private String 영업이익_당기 = "N/A";
    private String 영업이익_합계 = "N/A";
    private String 영업이익_평균 = "N/A";

    private String 유동자산합계 = "N/A";

    private String 유동부채합계 = "N/A";

    private String 유동비율 = "N/A";

    private String 투자자산_비유동자산내 = "N/A";

    private String 고정부채 = "N/A";

    private String 발행주식수 = "N/A";


    /** V2 추가 **/
    private String PER = "N/A";
    private String 영업이익성장률 = "N/A";
    private String 성장률보정PER = "N/A";
    private String 무형자산 = "N/A";

    private String 연구개발비_당기 ="N/A";
    private String 연구개발비_전기 = "N/A";
    private String 연구개발비_전전기 = "N/A";
    private String 연구개발비_평균 = "N/A";

    private String 총부채 = "N/A";
    private String 현금성자산 = "N/A";
    private String 순부채 = "N/A";

    /** 계산값 **/
    private String 계산_사업가치 = "N/A";
    private String 계산_재산가치 = "N/A";
    private String 계산_부채 = "N/A";
    private String 계산_기업가치 = "N/A";


    /** 기타값 **/
    private String 예외메세지_영업이익 = "N/A";
    private String 예외메시지_발행주식수 = "N/A";

    public CompanySharePriceResultDetail(String unit) {
        this.단위 = unit;
    }

}
