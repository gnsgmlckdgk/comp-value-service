package com.finance.dart.api.common.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 섹터별 주당가치 계산 파라미터
 */
@Data
@Builder
public class SectorCalculationParameters {

    /**
     * 섹터 기준 PER (Price-to-Earnings Ratio)
     */
    private BigDecimal basePER;

    /**
     * 무형자산 가중치 (0.0 ~ 1.0)
     */
    private BigDecimal intangibleAssetWeight;

    /**
     * PSR 상한 (Price-to-Sales Ratio)
     */
    private BigDecimal maxPSR;

    /**
     * 성장률 상한 (0.0 ~ 2.0, 예: 1.5 = 150%)
     */
    private BigDecimal growthRateCap;

    /**
     * 유동비율 적용 여부
     */
    private boolean applyCurrentRatio;

    /**
     * R&D 가중치 (연구개발비 중요도)
     */
    private BigDecimal rndWeight;

    /**
     * 섹터명
     */
    private String sectorName;

    /**
     * 그레이엄 PER 상한 (V7)
     */
    private BigDecimal maxPER;

    /**
     * 그레이엄 PBR 상한 (V7)
     */
    private BigDecimal maxPBR;

    /**
     * 그레이엄 PER×PBR 상한 (V7)
     */
    private BigDecimal maxPERxPBR;
}
