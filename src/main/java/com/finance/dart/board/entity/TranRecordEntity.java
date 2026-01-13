package com.finance.dart.board.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.finance.dart.member.entity.MemberEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 거래기록 테이블
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "tran_record",
       schema = "public",
       indexes = {
            @Index(name = "idx_tranrecord_member", columnList = "member_id")
       }
)
public class TranRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;               // PK

    @JsonIgnore // JSON 직렬화 시 순환 참조 방지
    @ManyToOne(fetch = FetchType.LAZY) // 여러 거래가 한 회원에 속할 수 있음 (N:1)
    @JoinColumn(name = "member_id", nullable = false) // FK 컬럼명 지정
    private MemberEntity member; // Member 테이블의 PK(id)를 참조

    @Column(nullable = false)
    private String symbol;         // 티커

    private String companyName;    // 기업명
    private String buyDate;        // 매수일자
    private Double buyPrice;       // 매수가격(단가)
    private Integer totalBuyAmount;// 수량

    private Double targetPrice;    // 매도목표가

    private Double buyExchangeRateAtTrade;  // 매수체결당시 환율

    @Column(length = 500)
    private String rmk;             // 비고

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
