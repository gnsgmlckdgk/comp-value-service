package com.finance.dart.cointrade.consts;

/**
 * <pre>
 * 머신러닝 코인자동매매 프로그램 설정
 * 파이썬
 * </pre>
 */
public class CoinTraderProgramConfig {

    // Server
    public static final String localHost = "http://localhost:18082";
    public static final String prodHost = "http://cointrader-service";


    // API

    /** 매수 프로세스 시작 시간 조회 API URI **/
    public static final String API_URI_BUY_NEXT_RUN = "api/scheduler/next-run";


}
