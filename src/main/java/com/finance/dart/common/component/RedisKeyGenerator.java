package com.finance.dart.common.component;

public class RedisKeyGenerator {

    /**
     * 해외기업 가치 계산 결과값
     * @param symbol    기업티커
     * @param version   계산함수버전(v1, v2, ...)
     * @return
     */
    public static String genAbroadCompValueRstData(String symbol, String version) {
        return "compvalue:abroad:calvalue:"+version+":"+symbol;
    }

}
