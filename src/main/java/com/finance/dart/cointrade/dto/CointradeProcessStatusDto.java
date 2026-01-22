package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class CointradeProcessStatusDto {

    /**
     *         {
     *             "status": "running" | "idle" | "finished",
     *             "mode" : "buy" | "sell" | None,
     *             "percent": 50, (0~100)
     *             "message": "현재 로그 메시지",
     *             "logs" : ["", "", ...],
     *             "last_updated": "YYYY-MM-DD HH:MM:SS"
     *         }
     */

    private String status;

    private String mode;

    private Double percent;

    private String message;

    private List<String> logs;

    @JsonProperty("last_updated")
    private String lastUpdated;

}
