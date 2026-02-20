package com.finance.dart.api.common.dto.evaluation;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

/**
 * 진입 타이밍 분석 결과 DTO
 * 단기 기술적 지표(SMA5/20, MACD, 볼린저밴드, 스토캐스틱, ATR) 기반
 */
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({
    "signal", "signalColor", "timingScore", "description",
    "shortTermTrend", "trendDetail",
    "estimatedSupport", "estimatedResistance",
    "sma5", "sma20", "macdLine", "macdSignal", "macdHistogram",
    "bollingerUpper", "bollingerMiddle", "bollingerLower",
    "stochasticK", "stochasticD", "atr"
})
public class EntryTimingAnalysis {

    /** 종합 시그널: "매수 적기" / "대기 권장" / "하락 구간" / "관망" */
    private String signal;

    /** 시그널 색상: "green" / "yellow" / "red" / "gray" */
    private String signalColor;

    /** 타이밍 점수 (0~100, 높을수록 진입에 유리) */
    private int timingScore;

    /** 상세 설명 */
    private String description;

    /** 단기 추세: "상승" / "하락" / "횡보" */
    private String shortTermTrend;

    /** 추세 상세 설명 */
    private String trendDetail;

    /** 예상 지지선 (볼린저밴드 하단 기반) */
    private double estimatedSupport;

    /** 예상 저항선 (볼린저밴드 상단 기반) */
    private double estimatedResistance;

    /** SMA 5일 */
    private double sma5;

    /** SMA 20일 */
    private double sma20;

    /** MACD Line (EMA12 - EMA26) */
    private double macdLine;

    /** MACD Signal (EMA9 of MACD) */
    private double macdSignal;

    /** MACD Histogram (MACD - Signal) */
    private double macdHistogram;

    /** 볼린저밴드 상단 */
    private double bollingerUpper;

    /** 볼린저밴드 중단 (SMA20) */
    private double bollingerMiddle;

    /** 볼린저밴드 하단 */
    private double bollingerLower;

    /** 스토캐스틱 %K */
    private double stochasticK;

    /** 스토캐스틱 %D */
    private double stochasticD;

    /** ATR (Average True Range, 14일) */
    private double atr;
}
