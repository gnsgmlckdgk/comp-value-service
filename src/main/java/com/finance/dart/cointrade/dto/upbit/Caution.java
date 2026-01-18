package com.finance.dart.cointrade.dto.upbit;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Caution {

    /** 가격 급등락 경부 **/
    private boolean PRICE_FLUCTUATIONS;

    /** 거래량 급증 경보 **/
    private boolean TRADING_VOLUME_SOARING;

    /** 입금량 급증 경보 **/
    private boolean DEPOSIT_AMOUNT_SOARING;

    /** 국내외 가격 차이 경보 **/
    private boolean GLOBAL_PRICE_DIFFERENCES;

    /** 소수 계정 집중 거래 경보 **/
    private boolean CONCENTRATION_OF_SMALL_ACCOUNTS;

}
