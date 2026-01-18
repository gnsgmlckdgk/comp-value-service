package com.finance.dart.cointrade.dto.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TradingParisDto {

    /** 페어(거래쌍)의 코드 / KRW-BTC **/
    private String market;

    @JsonProperty("korean_name")
    @SerializedName("korean_name")
    private String koreanName;

    @JsonProperty("english_name")
    @SerializedName("english_name")
    private String englishName;

    /** 종목 경보 정보 **/
    @JsonProperty("market_event")
    @SerializedName("market_event")
    private MarketEvent marketEvent;


}
