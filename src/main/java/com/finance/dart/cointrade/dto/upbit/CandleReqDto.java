package com.finance.dart.cointrade.dto.upbit;

import lombok.Data;

/**
 * 캔들(봉) 조회 요청 DTO
 */
@Data
public class CandleReqDto {

    /**
     * 마켓 코드 (예: KRW-BTC)
     * 필드: market (required)
     */
    private String market;

    /**
     * 마지막 캔들 시각 (ISO 8601 format, 예: 2023-01-01T00:00:00)
     * 비워두면 가장 최근 캔들 조회
     * 필드: to (optional)
     */
    private String to;

    /**
     * 캔들 개수 (최대 200개)
     * 기본값: 1
     * 필드: count (optional)
     */
    private Integer count;

    /**
     * 종가 환산 화폐 단위 (예: KRW)
     * 일(Day) 캔들에서만 사용됨
     * 필드: converting_price_unit (optional)
     */
    private String converting_price_unit;
}
