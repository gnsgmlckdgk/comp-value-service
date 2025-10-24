package com.finance.dart.board.dto;

import lombok.Data;

/**
 * 현재가 갱신 응답 DTO
 */
@Data
public class TranRecordCurValueResDto {

    /**
     * [
     *   { "symbol": "AAPL", "currentPrice": 231.12, "updatedAt": "2025-10-23T06:12:34Z" },
     *   { "symbol": "MSFT", "currentPrice": 413.88, "updatedAt": "2025-10-23T06:12:34Z" },
     *   { "symbol": "TSLA", "currentPrice": 258.01, "updatedAt": "2025-10-23T06:12:34Z" }
     * ]
     */

    private String symbol;          // 티커
    private Double currentPrice;    // 현재가
    private String updatedAt;       // 갱신시간(yyyy-MM-dd HH:mm:ss)

}
