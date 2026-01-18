package com.finance.dart.cointrade.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 코인 자동매매 거래 기록 Entity
 * 테이블: cointrade_trade_history
 */
@Data
@Entity
@Table(name = "cointrade_trade_history", schema = "public",
    indexes = {
        @Index(name = "idx_trade_history_coin_code", columnList = "coin_code"),
        @Index(name = "idx_trade_history_created_at", columnList = "created_at"),
        @Index(name = "idx_trade_history_trade_type", columnList = "trade_type")
    }
)
public class CointradeTradeHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "coin_code", nullable = false, length = 20)
    private String coinCode;

    @Column(name = "trade_type", nullable = false, length = 10)
    private String tradeType;

    @Column(name = "price", nullable = false, precision = 20, scale = 8)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false, precision = 20, scale = 8)
    private BigDecimal quantity;

    @Column(name = "total_amount", nullable = false, precision = 20, scale = 8)
    private BigDecimal totalAmount;

    @Column(name = "reason", length = 50)
    private String reason;

    @Column(name = "profit_loss", precision = 20, scale = 8)
    private BigDecimal profitLoss;

    @Column(name = "profit_loss_rate", precision = 10, scale = 4)
    private BigDecimal profitLossRate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
