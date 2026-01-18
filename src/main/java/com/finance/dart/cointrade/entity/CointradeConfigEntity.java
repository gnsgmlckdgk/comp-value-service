package com.finance.dart.cointrade.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 코인 자동매매 설정값 Entity
 * 테이블: cointrade_trading_config
 */
@Data
@Entity
@Table(name = "cointrade_trading_config", schema = "public")
public class CointradeConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "param_name", nullable = false, unique = true, length = 50)
    private String paramName;

    @Column(name = "param_value", nullable = false, length = 100)
    private String paramValue;

    @Column(name = "description", length = 200)
    private String description;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
