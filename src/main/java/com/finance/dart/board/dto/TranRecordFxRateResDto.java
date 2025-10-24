package com.finance.dart.board.dto;

import lombok.Data;

/**
 * 환율정보 갱신 응답 DTO
 */
@Data
public class TranRecordFxRateResDto {
    /**
     * { "rate": 1390.25, "updatedAt": "2025-10-23T06:12:34Z" }   // 1 USD → KRW
     */

    private String rate;        // 환율
    private String updatedAt;   // yyyy-MM-dd HH:mm:ss
}
