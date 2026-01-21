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

    /** 로그 목록 조회 API URI **/
    public static final String API_URI_LOG_FILES = "api/logs/files";

    /** 최신 로그 정보 조회 API URI **/
    public static final String API_URI_LOG_LATEST = "api/logs/latest";

    /** 특정 로그 파일 읽기 API URI (파일명을 뒤에 붙여서 사용) **/
    public static final String API_URI_LOG_READ = "api/logs/read";

    /** 증분 로그 조회 API URI (파일명을 뒤에 붙여서 사용) **/
    public static final String API_URI_LOG_INCREMENTAL = "api/logs/incremental";

    /** 매수 프로세스 수동 실행 **/
    public static final String API_URI_TRADE_BUY_START = "api/trade/buy/start";

    /** 매도 프로세스 수동 실행 **/
    public static final String API_URI_TRADE_SELL_START = "api/trade/sell/start";

    /** 매수/매도 프로세스 수동 중지 **/
    public static final String API_URI_TRADE_STOP = "api/trade/stop";

    /** 모델 학습 수동 실행 **/
    public static final String API_URI_MODEL_TRAIN = "api/model/train";

}
