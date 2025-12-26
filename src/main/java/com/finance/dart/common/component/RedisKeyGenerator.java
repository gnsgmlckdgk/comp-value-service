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

    /**
     * 추천 종목 전체 목록
     * @return compvalue:abroad:recommended:all
     */
    public static String genRecommendedStocksAll() {
        return "compvalue:abroad:recommended:all";
    }

    /**
     * 추천 종목 개별 데이터
     * @param symbol 기업티커
     * @return compvalue:abroad:recommended:stock:{symbol}
     */
    public static String genRecommendedStock(String symbol) {
        return "compvalue:abroad:recommended:stock:" + symbol;
    }

}
