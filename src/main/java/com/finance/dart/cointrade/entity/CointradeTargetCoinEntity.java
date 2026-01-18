package com.finance.dart.cointrade.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 코인 자동매매 대상 종목 Entity
 * 테이블: cointrade_target_coins
 */
@Data
@Entity
@Table(name = "cointrade_target_coins", schema = "public")
public class CointradeTargetCoinEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coin_code", nullable = false, unique = true, length = 20)
    private String coinCode;

    @Column(name = "coin_name", length = 100)
    private String coinName;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
