package com.finance.dart.board.dto;

import lombok.Data;

/**
 * 거래 기록 DTO
 */
@Data
public class TranRecordDto {

    /**
     * {
     *   "id": 123,                       // 고유 식별자 (number|string)
     *   "symbol": "AAPL",                // 티커 (string, 대소문자 무관)
     *   "companyName": "Apple Inc.",     // 기업명 (string, optional)
     *   "buyPrice": 170.25,              // 매수가(USD, number)
     *   "totalBuyAmount": 10,            // 수량 (number)  ← 현재 로직에서 수량으로 사용
     *   "buyDate": "2025-10-22",         // 매수일자 (YYYY-MM-DD 또는 ISO8601)
     *   "currentPrice": 231.1,           // 현재가(USD, number)  ※ 가능하면 포함 권장
     *   "targetPrice": 250               // 목표가(USD, number, optional)
     * }
     */

    private Long id;               // PK

    private String symbol;          // 티커
    private String companyName;     // 기업명
    private String buyDate;         // 매수일자
    private Double buyPrice;        // 매수가격(단가)
    private Integer totalBuyAmount; // 수량

    private Double currentPrice;    // 현재가
    private Double targetPrice;     // 매도목표가

    private String createdAt;
    private String updatedAt;

}
