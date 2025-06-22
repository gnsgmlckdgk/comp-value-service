package com.finance.dart.api.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stock_valuation_result", schema = "public")
public class StockValuationResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "base_date", nullable = false, length = 8)
    private String baseDate;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "company_code", nullable = false, length = 20)
    private String companyCode;

    @Column(name = "stock_code", length = 10)
    private String stockCode;

    @Column(name = "per_share_value")
    private Long perShareValue;

    @Column(name = "current_price")
    private Long currentPrice;

    @Column(name = "difference")
    private Long difference;

    @Column(name = "result_message", length = 200)
    private String resultMessage;

    @CreationTimestamp
    @Column(name = "checked_at", nullable = false, updatable = false)
    private LocalDateTime checkedAt;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

}
