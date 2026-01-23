package com.finance.dart.cointrade.dto.upbit;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 일(Day) 캔들 응답 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CandleDayResDto extends CandleCommonResDto {

    /**
     * 전일 종가 (UTC 0시 기준)
     */
    private Double prev_closing_price;

    /**
     * 전일 종가 대비 변경 금액
     */
    private Double change_price;

    /**
     * 전일 종가 대비 변경 비율
     */
    private Double change_rate;

    /**
     * 종가 환산 화폐 단위로 환산된 가격 (요청에 converting_price_unit 포함된 경우)
     */
    private Double converted_trade_price;
}
