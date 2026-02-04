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

    /** 스케줄러 설정 즉시 리로드 **/
    public static final String API_URI_SCHEDULER_RELOAD = "api/scheduler/reload";

    /** 프로세스 진행율 상태 조회 **/
    public static final String API_URI_PROCESS_STATUS_BUY = "api/process/status/buy";

    /** 매도 프로세스 진행율 상태 조회 **/
    public static final String API_URI_PROCESS_STATUS_SELL = "api/process/status/sell";

    /** 백테스트 실행 **/
    public static final String API_URI_BACKTEST_RUN = "api/backtest/run";

    /** 백테스트 작업 상태 조회 **/
    public static final String API_URI_BACKTEST_STATUS = "api/backtest/status";

    /** 백테스트 결과 조회 **/
    public static final String API_URI_BACKTEST_RESULT = "api/backtest/result";

    /** 백테스트 이력 조회 **/
    public static final String API_URI_BACKTEST_HISTORY = "api/backtest/history";

    /** 보유 종목 매도 **/
    public static final String API_URI_HOLDINGS_SELL = "api/holdings/sell";

    /** 백테스트 옵티마이저 실행 **/
    public static final String API_URI_OPTIMIZER_RUN = "api/backtest/optimizer";

    /** 백테스트 옵티마이저 상태 조회 **/
    public static final String API_URI_OPTIMIZER_STATUS = "api/backtest/optimizer/status";

    /** 백테스트 옵티마이저 결과 조회 **/
    public static final String API_URI_OPTIMIZER_RESULT = "api/backtest/optimizer/result";

    /** 백테스트 옵티마이저 이력 조회 **/
    public static final String API_URI_OPTIMIZER_HISTORY = "api/backtest/optimizer/history";

}
