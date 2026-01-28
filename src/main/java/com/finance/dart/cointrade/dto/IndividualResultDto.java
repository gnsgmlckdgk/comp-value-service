package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 백테스트 개별 종목 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualResultDto {

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

    @JsonProperty("avg_holding_days")
    private Double avgHoldingDays;

    @JsonProperty("profit_factor")
    private Double profitFactor;

    @JsonProperty("avg_profit")
    private Double avgProfit;

    @JsonProperty("avg_loss")
    private Double avgLoss;

    private List<TradeDto> trades;
}
