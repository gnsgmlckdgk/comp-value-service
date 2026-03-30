package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 백테스트 설정값 (모멘텀 스캘핑)
 */
@Getter
@Setter
@ToString
public class BacktestRequestConfigDto {

    // ── Capital ──

    /** 초기 자본금 (원, 기본값: DB 또는 1,000,000) */
    @JsonProperty("initial_capital")
    @SerializedName("initial_capital")
    private BigDecimal initialCapital;

    /** 1회 매수 금액 (원) */
    @JsonProperty("buy_amount_per_coin")
    @SerializedName("buy_amount_per_coin")
    private BigDecimal buyAmountPerCoin;

    /** 최대 동시 보유 종목 수 */
    @JsonProperty("buy_max_concurrent")
    @SerializedName("buy_max_concurrent")
    private Integer buyMaxConcurrent;

    // ── Scanner ──

    /** 스캐너 거래량 비율 최소값 */
    @JsonProperty("scanner_volume_ratio_min")
    @SerializedName("scanner_volume_ratio_min")
    private BigDecimal scannerVolumeRatioMin;

    /** 스캐너 가격 변동 최소값 (%) */
    @JsonProperty("scanner_price_change_min")
    @SerializedName("scanner_price_change_min")
    private BigDecimal scannerPriceChangeMin;

    /** 스캐너 RSI 최소값 */
    @JsonProperty("scanner_rsi_min")
    @SerializedName("scanner_rsi_min")
    private BigDecimal scannerRsiMin;

    /** 스캐너 RSI 최대값 */
    @JsonProperty("scanner_rsi_max")
    @SerializedName("scanner_rsi_max")
    private BigDecimal scannerRsiMax;

    /** 스캐너 VWAP 편차 최대값 */
    @JsonProperty("scanner_vwap_deviation_max")
    @SerializedName("scanner_vwap_deviation_max")
    private BigDecimal scannerVwapDeviationMax;

    /** 스캐너 룩백 기간 (분) */
    @JsonProperty("scanner_lookback_minutes")
    @SerializedName("scanner_lookback_minutes")
    private Integer scannerLookbackMinutes;

    /** 스캐너 실행 간격 (초) */
    @JsonProperty("scanner_interval_seconds")
    @SerializedName("scanner_interval_seconds")
    private Integer scannerIntervalSeconds;

    // ── ML ──

    /** ML 모델 활성화 여부 */
    @JsonProperty("ml_enabled")
    @SerializedName("ml_enabled")
    private Boolean mlEnabled;

    /** ML 최소 신뢰도 */
    @JsonProperty("ml_min_confidence")
    @SerializedName("ml_min_confidence")
    private BigDecimal mlMinConfidence;

    /** ML 캔들 단위 (분) */
    @JsonProperty("ml_candle_unit")
    @SerializedName("ml_candle_unit")
    private Integer mlCandleUnit;

    /** ML 학습 룩백 캔들 수 */
    @JsonProperty("ml_train_lookback_candles")
    @SerializedName("ml_train_lookback_candles")
    private Integer mlTrainLookbackCandles;

    // ── Sell ──

    /** 익절 비율 (%) */
    @JsonProperty("take_profit_pct")
    @SerializedName("take_profit_pct")
    private BigDecimal takeProfitPct;

    /** 손절 비율 (%) */
    @JsonProperty("stop_loss_pct")
    @SerializedName("stop_loss_pct")
    private BigDecimal stopLossPct;

    /** 트레일링 스탑 활성화 여부 */
    @JsonProperty("trailing_stop_enabled")
    @SerializedName("trailing_stop_enabled")
    private Boolean trailingStopEnabled;

    /** 트레일링 스탑 활성화 수익률 (%) */
    @JsonProperty("trailing_stop_activation_pct")
    @SerializedName("trailing_stop_activation_pct")
    private BigDecimal trailingStopActivationPct;

    /** 트레일링 스탑 하락률 (%) */
    @JsonProperty("trailing_stop_rate_pct")
    @SerializedName("trailing_stop_rate_pct")
    private BigDecimal trailingStopRatePct;

    /** 최대 보유 시간 (분) */
    @JsonProperty("max_hold_minutes")
    @SerializedName("max_hold_minutes")
    private Integer maxHoldMinutes;

    /** 매수 쿨다운 시간 (분) */
    @JsonProperty("buy_cooldown_minutes")
    @SerializedName("buy_cooldown_minutes")
    private Integer buyCooldownMinutes;

    // ── BTC Filter ──

    /** BTC 추세 필터 활성화 여부 */
    @JsonProperty("btc_filter_enabled")
    @SerializedName("btc_filter_enabled")
    private Boolean btcFilterEnabled;

    /** BTC 추세 MA 기간 */
    @JsonProperty("btc_trend_ma_period")
    @SerializedName("btc_trend_ma_period")
    private Integer btcTrendMaPeriod;

    // ── Fee ──

    /** 매매 수수료율 (매수/매도 동일) */
    @JsonProperty("trading_fee_rate")
    @SerializedName("trading_fee_rate")
    private BigDecimal tradingFeeRate;

}
