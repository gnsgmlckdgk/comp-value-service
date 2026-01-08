package com.finance.dart.api.common.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 프로파일별 스크리닝 설정 엔티티
 */
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "tb_recommend_profile_config", schema = "public")
public class RecommendProfileConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                        // PK

    /**
     * 프로파일 (FK)
     * - 프로파일 삭제 시 설정도 함께 삭제됨 (부모 엔티티에서 cascade 설정)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private RecommendProfileEntity profile;

    // ========== Stock Screener 조건 ==========

    @Column(name = "market_cap_min")
    private Long marketCapMin;              // 시가총액 최소 (USD)

    @Column(name = "market_cap_max")
    private Long marketCapMax;              // 시가총액 최대 (USD)

    @Column(name = "beta_max", precision = 5, scale = 2)
    private BigDecimal betaMax;             // 베타 최대값 (변동성 지표)

    @Column(name = "volume_min")
    private Long volumeMin;                 // 거래량 최소

    @Column(name = "is_etf", length = 1)
    private String isEtf = "N";             // ETF 포함여부 (Y/N)

    @Column(name = "is_fund", length = 1)
    private String isFund = "N";            // 펀드 포함여부 (Y/N)

    @Column(name = "is_actively_trading", length = 1)
    private String isActivelyTrading = "Y"; // 활성거래 종목만 (Y/N)

    @Column(name = "exchange", length = 100)
    private String exchange;                // 거래소 (콤마구분, 예: NYSE,NASDAQ)

    @Column(name = "screener_limit")
    private Integer screenerLimit = 10000;  // 스크리너 조회 제한 건수

    // ========== 저평가 필터링 조건 ==========

    @Column(name = "pe_ratio_min", precision = 10, scale = 2)
    private BigDecimal peRatioMin;          // PER 최소값 (주가수익비율)

    @Column(name = "pe_ratio_max", precision = 10, scale = 2)
    private BigDecimal peRatioMax;          // PER 최대값

    @Column(name = "pb_ratio_max", precision = 10, scale = 2)
    private BigDecimal pbRatioMax;          // PBR 최대값 (주가순자산비율)

    @Column(name = "roe_min", precision = 10, scale = 4)
    private BigDecimal roeMin;              // ROE 최소값 (자기자본이익률, 0.10 = 10%)

    @Column(name = "debt_equity_max", precision = 10, scale = 2)
    private BigDecimal debtEquityMax;       // 부채비율 최대값 (D/E Ratio)

    // ========== 공통 ==========

    private LocalDateTime createdAt = LocalDateTime.now();  // 등록일시
    private LocalDateTime updatedAt = LocalDateTime.now();  // 수정일시

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
