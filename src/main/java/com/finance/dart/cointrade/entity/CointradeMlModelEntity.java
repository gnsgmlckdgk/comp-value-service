package com.finance.dart.cointrade.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 코인 자동매매 ML 모델 정보 Entity
 * 테이블: cointrade_ml_models
 */
@Data
@Entity
@Table(name = "cointrade_ml_models", schema = "public")
public class CointradeMlModelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coin_code", nullable = false, unique = true, length = 20)
    private String coinCode;

    @Column(name = "model_path", nullable = false, length = 200)
    private String modelPath;

    @Column(name = "trained_at", nullable = false)
    private LocalDateTime trainedAt;

    @Column(name = "train_data_start")
    private LocalDateTime trainDataStart;

    @Column(name = "train_data_end")
    private LocalDateTime trainDataEnd;

    @Column(name = "accuracy", precision = 6, scale = 4)
    private BigDecimal accuracy;

    @Column(name = "auc_roc", precision = 6, scale = 4)
    private BigDecimal aucRoc;

    @Column(name = "feature_count")
    private Integer featureCount;

    @Column(name = "train_samples")
    private Integer trainSamples;

    @Column(name = "candle_unit", length = 10)
    private String candleUnit;

    @Column(name = "model_type", length = 20)
    private String modelType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
