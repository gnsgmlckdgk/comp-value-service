package com.finance.dart.api.common.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 추천종목 프로파일 DTO (마스터 + 설정 통합)
 */
@Data
public class RecommendProfileDto {

    // ========== 프로파일 마스터 ==========

    private Long id;                        // PK
    private String profileName;             // 프로파일명 (예: 가치투자형, 성장주형)
    private String profileDesc;             // 프로파일 설명
    private String isActive;                // 활성여부 (Y: 스케줄러에서 사용, N: 미사용)
    private Integer sortOrder;              // 정렬순서
    private String useYn;                   // 사용여부 (Y: 사용, N: 삭제처리)
    private String createdAt;               // 등록일시
    private String updatedAt;               // 수정일시

    // ========== Stock Screener 조건 ==========

    private Long marketCapMin;              // 시가총액 최소 (USD)
    private Long marketCapMax;              // 시가총액 최대 (USD)
    private BigDecimal betaMax;             // 베타 최대값 (변동성 지표)
    private Long volumeMin;                 // 거래량 최소
    private String isEtf;                   // ETF 포함여부 (Y/N)
    private String isFund;                  // 펀드 포함여부 (Y/N)
    private String isActivelyTrading;       // 활성거래 종목만 (Y/N)
    private String exchange;                // 거래소 (콤마구분, 예: NYSE,NASDAQ)
    private Integer screenerLimit;          // 스크리너 조회 제한 건수

    // ========== 저평가 필터링 조건 ==========

    private BigDecimal peRatioMin;          // PER 최소값 (주가수익비율)
    private BigDecimal peRatioMax;          // PER 최대값
    private BigDecimal pbRatioMax;          // PBR 최대값 (주가순자산비율)
    private BigDecimal roeMin;              // ROE 최소값 (자기자본이익률, 0.10 = 10%)
    private BigDecimal debtEquityMax;       // 부채비율 최대값 (D/E Ratio)
}
