package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 백테스트 포트폴리오 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioDto {

    @JsonProperty("coin_codes")
    private List<String> coinCodes;

    @JsonProperty("coin_count")
    private Integer coinCount;

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("initial_capital")
    private Double initialCapital;

    @JsonProperty("final_capital")
    private Double finalCapital;

    @JsonProperty("total_return")
    private Double totalReturn;

    @JsonProperty("total_trades")
    private Integer totalTrades;

    @JsonProperty("winning_trades")
    private Integer winningTrades;

    @JsonProperty("losing_trades")
    private Integer losingTrades;

    @JsonProperty("win_rate")
    private Double winRate;

    @JsonProperty("max_drawdown")
    private Double maxDrawdown;

    @JsonProperty("sharpe_ratio")
    private Double sharpeRatio;
}
