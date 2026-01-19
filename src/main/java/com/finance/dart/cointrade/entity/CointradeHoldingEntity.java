package com.finance.dart.cointrade.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 코인 자동매매 보유 종목 Entity
 * 테이블: cointrade_holdings
 */
@Data
@Entity
@Table(name = "cointrade_holdings", schema = "public")
public class CointradeHoldingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coin_code", nullable = false, unique = true, length = 20)
    private String coinCode;

    @Column(name = "buy_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal buyPrice;

    @Column(name = "quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(name = "total_amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal totalAmount;

    @Column(name = "predicted_high", nullable = false, precision = 20, scale = 8)
    private BigDecimal predictedHigh;

    @Column(name = "predicted_low", nullable = false, precision = 20, scale = 8)
    private BigDecimal predictedLow;

    @Column(name = "buy_date")
    private LocalDateTime buyDate;

    @Column(name = "surge_probability", nullable = false, precision = 5, scale = 2)
    private BigDecimal surgeProbability; // 급등 확률

    @Column(name = "surge_day")
    private LocalDate surgeDay; // 급등 예상일

    @Column(name = "expire_date")
    private LocalDate expireDate; // 만료일

    @Column(name = "buy_score", nullable = false, precision = 5, scale = 2)
    private BigDecimal buyScore; // 매수 점수


    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
