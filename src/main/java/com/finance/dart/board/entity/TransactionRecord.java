package com.finance.dart.board.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finance.dart.member.entity.MemberEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 거래기록 테이블
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "tran_record", schema = "public")
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;               // PK

    @ManyToOne(fetch = FetchType.LAZY) // 여러 거래가 한 회원에 속할 수 있음 (N:1)
    @JoinColumn(name = "member_id", nullable = false) // FK 컬럼명 지정
    private MemberEntity memberEntity; // Member 테이블의 PK(id)를 참조

    @Column(nullable = false, unique = true)
    private String symbol;         // 티커

    private String companyName;    // 기업명
    private Double buyPrice;       // 매수가격(단가)
    private Integer totalBuyAmount;// 총매수가격(또는 수량: 현재 UI 의미에 맞게 사용)
    private LocalDate buyDate;     // 매수일자
    private Double currentPrice;   // 현재가격
    private Double targetPrice;    // 매도목표가

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
