package com.finance.dart.board.dto;

import lombok.Data;

/**
 * 매도 기록 DTO
 */
@Data
public class SellRecordDto {

    private Long id;                    // PK

    // --- 거래 기록 정보
    private String symbol;              // 티커
    private String companyName;         // 기업명

    // --- 매도 정보
    private String sellDate;            // 매도일자
    private Double sellPrice;           // 매도가격(단가)
    private Integer sellQty;            // 매도수량
    private Double realizedPnl;         // 실현손익

    private String rmk;                 // 비고

    private String createdAt;           // 등록일자
    private String updatedAt;           // 수정일자
}
