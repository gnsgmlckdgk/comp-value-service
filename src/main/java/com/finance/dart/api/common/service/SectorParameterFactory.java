package com.finance.dart.api.common.service;

import com.finance.dart.api.common.dto.SectorCalculationParameters;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 섹터별 계산 파라미터 팩토리
 */
@Slf4j
public class SectorParameterFactory {

    private static final Map<String, SectorCalculationParameters> SECTOR_PARAMS = new HashMap<>();

    static {
        // Technology (기술)
        SECTOR_PARAMS.put("Technology", SectorCalculationParameters.builder()
                .sectorName("Technology")
                .basePER(new BigDecimal("30"))
                .intangibleAssetWeight(new BigDecimal("0.6"))
                .maxPSR(new BigDecimal("20"))
                .growthRateCap(new BigDecimal("1.5"))
                .applyCurrentRatio(true)
                .rndWeight(new BigDecimal("1.0"))
                .maxPER(new BigDecimal("35"))
                .maxPBR(new BigDecimal("10"))
                .maxPERxPBR(new BigDecimal("200"))
                .build());

        // Communication Services (통신 서비스)
        SECTOR_PARAMS.put("Communication Services", SectorCalculationParameters.builder()
                .sectorName("Communication Services")
                .basePER(new BigDecimal("25"))
                .intangibleAssetWeight(new BigDecimal("0.5"))
                .maxPSR(new BigDecimal("15"))
                .growthRateCap(new BigDecimal("1.2"))
                .applyCurrentRatio(true)
                .rndWeight(new BigDecimal("0.8"))
                .maxPER(new BigDecimal("28"))
                .maxPBR(new BigDecimal("6"))
                .maxPERxPBR(new BigDecimal("120"))
                .build());

        // Healthcare (헬스케어)
        SECTOR_PARAMS.put("Healthcare", SectorCalculationParameters.builder()
                .sectorName("Healthcare")
                .basePER(new BigDecimal("25"))
                .intangibleAssetWeight(new BigDecimal("0.5"))
                .maxPSR(new BigDecimal("15"))
                .growthRateCap(new BigDecimal("1.0"))
                .applyCurrentRatio(true)
                .rndWeight(new BigDecimal("1.2"))
                .maxPER(new BigDecimal("28"))
                .maxPBR(new BigDecimal("6"))
                .maxPERxPBR(new BigDecimal("120"))
                .build());

        // Consumer Cyclical (임의 소비재)
        SECTOR_PARAMS.put("Consumer Cyclical", SectorCalculationParameters.builder()
                .sectorName("Consumer Cyclical")
                .basePER(new BigDecimal("18"))
                .intangibleAssetWeight(new BigDecimal("0.35"))
                .maxPSR(new BigDecimal("5"))
                .growthRateCap(new BigDecimal("0.8"))
                .applyCurrentRatio(true)
                .rndWeight(new BigDecimal("0.5"))
                .maxPER(new BigDecimal("22"))
                .maxPBR(new BigDecimal("4"))
                .maxPERxPBR(new BigDecimal("66"))
                .build());

        // Consumer Defensive (필수 소비재)
        SECTOR_PARAMS.put("Consumer Defensive", SectorCalculationParameters.builder()
                .sectorName("Consumer Defensive")
                .basePER(new BigDecimal("20"))
                .intangibleAssetWeight(new BigDecimal("0.3"))
                .maxPSR(new BigDecimal("3"))
                .growthRateCap(new BigDecimal("0.3"))
                .applyCurrentRatio(true)
                .rndWeight(new BigDecimal("0.3"))
                .maxPER(new BigDecimal("22"))
                .maxPBR(new BigDecimal("4"))
                .maxPERxPBR(new BigDecimal("66"))
                .build());

        // Industrials (산업재)
        SECTOR_PARAMS.put("Industrials", SectorCalculationParameters.builder()
                .sectorName("Industrials")
                .basePER(new BigDecimal("18"))
                .intangibleAssetWeight(new BigDecimal("0.15"))
                .maxPSR(new BigDecimal("2"))
                .growthRateCap(new BigDecimal("0.5"))
                .applyCurrentRatio(true)
                .rndWeight(new BigDecimal("0.6"))
                .maxPER(new BigDecimal("20"))
                .maxPBR(new BigDecimal("3"))
                .maxPERxPBR(new BigDecimal("45"))
                .build());

        // Financial Services (금융)
        SECTOR_PARAMS.put("Financial Services", SectorCalculationParameters.builder()
                .sectorName("Financial Services")
                .basePER(new BigDecimal("12"))
                .intangibleAssetWeight(new BigDecimal("0.05"))
                .maxPSR(new BigDecimal("3"))
                .growthRateCap(new BigDecimal("0.4"))
                .applyCurrentRatio(false)  // 금융업은 유동비율 적용 안함
                .rndWeight(new BigDecimal("0.1"))
                .maxPER(new BigDecimal("15"))
                .maxPBR(new BigDecimal("2"))
                .maxPERxPBR(new BigDecimal("22.5"))
                .build());

        // Real Estate (부동산)
        SECTOR_PARAMS.put("Real Estate", SectorCalculationParameters.builder()
                .sectorName("Real Estate")
                .basePER(new BigDecimal("20"))
                .intangibleAssetWeight(new BigDecimal("0.1"))
                .maxPSR(new BigDecimal("5"))
                .growthRateCap(new BigDecimal("0.4"))
                .applyCurrentRatio(true)
                .rndWeight(new BigDecimal("0.1"))
                .maxPER(new BigDecimal("22"))
                .maxPBR(new BigDecimal("3"))
                .maxPERxPBR(new BigDecimal("50"))
                .build());

        // Energy (에너지)
        SECTOR_PARAMS.put("Energy", SectorCalculationParameters.builder()
                .sectorName("Energy")
                .basePER(new BigDecimal("15"))
                .intangibleAssetWeight(new BigDecimal("0.1"))
                .maxPSR(new BigDecimal("2"))
                .growthRateCap(new BigDecimal("0.6"))
                .applyCurrentRatio(true)
                .rndWeight(new BigDecimal("0.4"))
                .maxPER(new BigDecimal("18"))
                .maxPBR(new BigDecimal("2.5"))
                .maxPERxPBR(new BigDecimal("33"))
                .build());

        // Basic Materials (기초 소재)
        SECTOR_PARAMS.put("Basic Materials", SectorCalculationParameters.builder()
                .sectorName("Basic Materials")
                .basePER(new BigDecimal("15"))
                .intangibleAssetWeight(new BigDecimal("0.1"))
                .maxPSR(new BigDecimal("2"))
                .growthRateCap(new BigDecimal("0.5"))
                .applyCurrentRatio(true)
                .rndWeight(new BigDecimal("0.5"))
                .maxPER(new BigDecimal("18"))
                .maxPBR(new BigDecimal("2.5"))
                .maxPERxPBR(new BigDecimal("33"))
                .build());

        // Utilities (유틸리티)
        SECTOR_PARAMS.put("Utilities", SectorCalculationParameters.builder()
                .sectorName("Utilities")
                .basePER(new BigDecimal("15"))
                .intangibleAssetWeight(new BigDecimal("0.1"))
                .maxPSR(new BigDecimal("2"))
                .growthRateCap(new BigDecimal("0.2"))
                .applyCurrentRatio(true)
                .rndWeight(new BigDecimal("0.2"))
                .maxPER(new BigDecimal("18"))
                .maxPBR(new BigDecimal("2.5"))
                .maxPERxPBR(new BigDecimal("33"))
                .build());
    }

    /**
     * 기본 파라미터 (섹터 정보가 없거나 매칭되지 않을 때)
     */
    private static final SectorCalculationParameters DEFAULT_PARAMS = SectorCalculationParameters.builder()
            .sectorName("Default")
            .basePER(new BigDecimal("20"))
            .intangibleAssetWeight(new BigDecimal("0.3"))
            .maxPSR(new BigDecimal("10"))
            .growthRateCap(new BigDecimal("1.0"))
            .applyCurrentRatio(true)
            .rndWeight(new BigDecimal("0.5"))
            .maxPER(new BigDecimal("20"))
            .maxPBR(new BigDecimal("3"))
            .maxPERxPBR(new BigDecimal("45"))
            .build();

    /**
     * 섹터에 맞는 계산 파라미터를 반환
     *
     * @param sector 섹터명
     * @return 섹터별 계산 파라미터
     */
    public static SectorCalculationParameters getParameters(String sector) {
        if (sector == null || sector.trim().isEmpty()) {
            if(log.isDebugEnabled()) log.debug("[섹터 파라미터] 섹터 정보 없음 - 기본값 사용");
            return DEFAULT_PARAMS;
        }

        SectorCalculationParameters params = SECTOR_PARAMS.get(sector);
        if (params == null) {
            if(log.isDebugEnabled()) log.debug("[섹터 파라미터] 섹터 '{}' 매칭 안됨 - 기본값 사용", sector);
            return DEFAULT_PARAMS;
        }

        if(log.isDebugEnabled()) log.debug("[섹터 파라미터] 섹터 '{}' - PER:{}, 무형자산:{}, PSR상한:{}",
            sector, params.getBasePER(), params.getIntangibleAssetWeight(), params.getMaxPSR());
        return params;
    }
}
