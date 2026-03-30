package com.finance.dart.cointrade.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 코인 자동매매 스캐너 시그널 Entity
 * 테이블: cointrade_scanner_signals
 */
@Data
@Entity
@Table(name = "cointrade_scanner_signals", schema = "public",
    indexes = {
        @Index(name = "idx_scanner_signal_coin", columnList = "coin_code"),
        @Index(name = "idx_scanner_signal_detected", columnList = "detected_at"),
        @Index(name = "idx_scanner_signal_action", columnList = "action_taken")
    }
)
public class CointradeScannerSignalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coin_code", length = 20)
    private String coinCode;

    @Column(name = "detected_at")
    private LocalDateTime detectedAt;

    @Column(name = "signal_type", length = 30)
    private String signalType;

    @Column(name = "momentum_score", precision = 6, scale = 4)
    private BigDecimal momentumScore;

    @Column(name = "volume_ratio", precision = 10, scale = 2)
    private BigDecimal volumeRatio;

    @Column(name = "price_change_pct", precision = 8, scale = 4)
    private BigDecimal priceChangePct;

    @Column(name = "rsi_value", precision = 6, scale = 2)
    private BigDecimal rsiValue;

    @Column(name = "vwap_deviation", precision = 8, scale = 4)
    private BigDecimal vwapDeviation;

    @Column(name = "ml_confidence", precision = 6, scale = 4)
    private BigDecimal mlConfidence;

    @Column(name = "action_taken", length = 20)
    private String actionTaken;

    @Column(name = "current_price", precision = 20, scale = 8)
    private BigDecimal currentPrice;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
