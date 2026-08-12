package com.finance.dart.api.common.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 수익률 추적 집계 결과 (2-4)
 * - 특정 기준일의 투자판정/가치등급 그룹이 최신 시점까지 낸 평균 수익률·승률
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceSummaryDto {

    /** 그룹 키 (투자판정 또는 가치등급 값) */
    private String group;

    /** 종목 수 (기준일·최신일 모두 가격이 있는 종목) */
    private int count;

    /** 평균 수익률 (%) */
    private double avgReturnPct;

    /** 승률 (수익률 > 0 비율, %) */
    private double winRatePct;

    /** 최고 수익률 (%) */
    private double maxReturnPct;

    /** 최저 수익률 (%) */
    private double minReturnPct;
}
