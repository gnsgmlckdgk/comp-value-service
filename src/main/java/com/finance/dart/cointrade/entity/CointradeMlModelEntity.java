package com.finance.dart.cointrade.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private LocalDate trainDataStart;

    @Column(name = "train_data_end")
    private LocalDate trainDataEnd;

    @Column(name = "mse", precision = 20, scale = 10)
    private BigDecimal mse;

    @Column(name = "mae", precision = 20, scale = 10)
    private BigDecimal mae;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
