package com.finance.dart.stockpredictor.consts;

/**
 * <pre>
 * 머신러닝 주식 가격 예측 프로그램 설정
 * 파이썬
 * </pre>
 */
public class PredictionProgramConfig {

    // Server
    public static final String localHost = "http://localhost:18081";
    public static final String prodHost = "http://stock-predictor-service";


    // API

    /** 예측 API URI **/
    public static final String API_URI_predict = "predict";

    /** 로그 API URI **/
    public static final String API_URI_logs = "logs";
    public static final String API_URI_logs_latest = "logs/latest";
    public static final String API_URI_logs_stream_latest = "logs/stream/latest";

    /** 재학습 API URI **/
    public static final String API_URI_retrain = "retrain";
    public static final String API_URI_retrain_status = "retrain/status";
    public static final String API_URI_retrain_targets = "retrain/targets";

}
