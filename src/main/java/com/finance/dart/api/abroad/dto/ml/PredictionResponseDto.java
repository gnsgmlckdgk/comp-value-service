package com.finance.dart.api.abroad.dto.ml;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * AI 예측 조회 응답 DTO
 */
@Getter
@Setter
@ToString
@Builder
public class PredictionResponseDto {

    /** 티커 심볼 */
    private String ticker;

    /** 기업명 */
    private String companyName;

    /** 거래소 */
    private String exchange;

    /** 예측 최고가 (USD) */
    private BigDecimal predictedHigh;

    /** 예측 시점 현재가 (USD) */
    private BigDecimal currentPrice;

    /** 상승 여력 (%) */
    private String upsidePercent;

    /** 예측 날짜 */
    private String predictionDate;

    /** 예측 대상 기간 시작일 */
    private String targetStartDate;

    /** 예측 대상 기간 종료일 (1주 후) */
    private String targetEndDate;

    /** 데이터 소스 (database: DB 저장된 데이터, realtime: 실시간 예측 데이터) */
    private String source;

    /** 모델 버전 */
    private String modelVersion;
}
