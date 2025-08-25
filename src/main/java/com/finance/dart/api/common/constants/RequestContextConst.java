package com.finance.dart.api.common.constants;

import lombok.Data;


/**
 * Request Scope 데이터 저장 객체 키 값
 */
@Data
public class RequestContextConst {

    // 시작: CompanySharePriceResultDetail --------------------------
    public static final String 영업이익_합계 = "영업이익_합계";
    public static final String 영업이익_평균 = "영업이익_평균";

    public static final String 계산_사업가치 = "계산_사업가치";
    public static final String 계산_재산가치 = "계산_재산가치";
    public static final String 계산_부채 = "계산_부채";
    public static final String 계산_기업가치 = "계산_기업가치";
    // 끝: CompanySharePriceResultDetail --------------------------

}
