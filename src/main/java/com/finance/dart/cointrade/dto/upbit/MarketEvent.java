package com.finance.dart.cointrade.dto.upbit;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MarketEvent {

    /** 유의 종목 여부 **/
    private boolean warning;

    /** 주의 종목 여부 **/
    private Caution caution;

}
