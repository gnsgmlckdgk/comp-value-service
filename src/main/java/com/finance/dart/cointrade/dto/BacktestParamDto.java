package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 백테스트 파라미터 DTO (결과 조회 시 사용, 모멘텀 스캘핑)
 */
@Getter
@Setter
@ToString
public class BacktestParamDto {

    // ── Capital ──

    @JsonProperty("initial_capital")
    private BigDecimal initialCapital;

    @JsonProperty("buy_amount_per_coin")
    private BigDecimal buyAmountPerCoin;

    @JsonProperty("buy_max_concurrent")
    private Integer buyMaxConcurrent;

    // ── Scanner ──

    @JsonProperty("scanner_volume_ratio_min")
    private BigDecimal scannerVolumeRatioMin;

    @JsonProperty("scanner_price_change_min")
    private BigDecimal scannerPriceChangeMin;

    @JsonProperty("scanner_rsi_min")
    private BigDecimal scannerRsiMin;

    @JsonProperty("scanner_rsi_max")
    private BigDecimal scannerRsiMax;

    @JsonProperty("scanner_vwap_deviation_max")
    private BigDecimal scannerVwapDeviationMax;

    @JsonProperty("scanner_lookback_minutes")
    private Integer scannerLookbackMinutes;

    @JsonProperty("scanner_interval_seconds")
    private Integer scannerIntervalSeconds;

    // ── ML ──

    @JsonProperty("ml_enabled")
    private Boolean mlEnabled;

    @JsonProperty("ml_min_confidence")
    private BigDecimal mlMinConfidence;

    @JsonProperty("ml_candle_unit")
    private Integer mlCandleUnit;

    @JsonProperty("ml_train_lookback_candles")
    private Integer mlTrainLookbackCandles;

    // ── Sell ──

    @JsonProperty("take_profit_pct")
    private BigDecimal takeProfitPct;

    @JsonProperty("stop_loss_pct")
    private BigDecimal stopLossPct;

    @JsonProperty("trailing_stop_enabled")
    private Boolean trailingStopEnabled;

    @JsonProperty("trailing_stop_activation_pct")
    private BigDecimal trailingStopActivationPct;

    @JsonProperty("trailing_stop_rate_pct")
    private BigDecimal trailingStopRatePct;

    @JsonProperty("max_hold_minutes")
    private Integer maxHoldMinutes;

    @JsonProperty("buy_cooldown_minutes")
    private Integer buyCooldownMinutes;

    // ── BTC Filter ──

    @JsonProperty("btc_filter_enabled")
    private Boolean btcFilterEnabled;

    @JsonProperty("btc_trend_ma_period")
    private Integer btcTrendMaPeriod;

    // ── Fee ──

    @JsonProperty("trading_fee_rate")
    private BigDecimal tradingFeeRate;
}
