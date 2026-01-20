package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CointradeNextRunDto {

    private String status;

    @JsonProperty("next_run_time")
    private String nextRunTime;

    @JsonProperty("remaining_seconds")
    private String remainingSeconds;

    @JsonProperty("display_message")
    private String displayMessage;

}
