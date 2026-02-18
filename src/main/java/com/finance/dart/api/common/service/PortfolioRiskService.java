package com.finance.dart.api.common.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * V8: 포트폴리오 리스크 관리 서비스
 * - 하드 손절: 매수가 대비 -20%
 * - 트레일링 스탑: 매수 후 고점 대비 -10%
 * - 재평가 신호: 평가 점수가 C등급(55) 미만
 */
@Slf4j
@AllArgsConstructor
@Service
public class PortfolioRiskService {

    /**
     * 리스크 신호 타입
     */
    public enum RiskSignal {
        HOLD,           // 보유 유지
        REVIEW,         // 재평가 필요
        TRAILING_STOP,  // 트레일링 스탑 발동
        STOP_LOSS       // 하드 손절
    }

    /**
     * 포지션 리스크 평가 결과
     */
    @Getter
    @ToString
    public static class RiskAssessment {
        private final RiskSignal signal;
        private final String message;
        private final String stopLossPrice;      // 하드 손절가
        private final String trailingStopPrice;  // 트레일링 스탑가

        public RiskAssessment(RiskSignal signal, String message, String stopLossPrice, String trailingStopPrice) {
            this.signal = signal;
            this.message = message;
            this.stopLossPrice = stopLossPrice;
            this.trailingStopPrice = trailingStopPrice;
        }
    }

    /**
     * 포지션 리스크 평가
     * @param purchasePrice 매수가
     * @param currentPrice 현재가
     * @param highSincePurchase 매수 후 고점 (없으면 매수가 사용)
     * @param evaluationScore 현재 평가 점수 (없으면 -1)
     * @return 리스크 평가 결과
     */
    public RiskAssessment assessRisk(double purchasePrice, double currentPrice,
                                      double highSincePurchase, double evaluationScore) {

        if (purchasePrice <= 0 || currentPrice <= 0) {
            return new RiskAssessment(RiskSignal.HOLD, "가격 정보 부족", "N/A", "N/A");
        }

        // 하드 손절가 = 매수가 × 0.8 (-20%)
        BigDecimal stopLoss = BigDecimal.valueOf(purchasePrice)
                .multiply(new BigDecimal("0.8"))
                .setScale(2, RoundingMode.HALF_UP);

        // 트레일링 스탑가 = 고점 × 0.9 (-10%)
        double effectiveHigh = Math.max(highSincePurchase, purchasePrice);
        BigDecimal trailingStop = BigDecimal.valueOf(effectiveHigh)
                .multiply(new BigDecimal("0.9"))
                .setScale(2, RoundingMode.HALF_UP);

        String stopLossStr = stopLoss.toPlainString();
        String trailingStopStr = trailingStop.toPlainString();

        // 1. 하드 손절 체크: 현재가 <= 매수가 × 0.8
        if (currentPrice <= stopLoss.doubleValue()) {
            double lossPct = (1.0 - currentPrice / purchasePrice) * 100;
            return new RiskAssessment(
                    RiskSignal.STOP_LOSS,
                    String.format("하드 손절 발동: 매수가($%.2f) 대비 -%.1f%% 하락. 즉시 매도 권장.", purchasePrice, lossPct),
                    stopLossStr,
                    trailingStopStr
            );
        }

        // 2. 트레일링 스탑 체크: 현재가 <= 고점 × 0.9
        if (currentPrice <= trailingStop.doubleValue() && effectiveHigh > purchasePrice) {
            double dropFromHigh = (1.0 - currentPrice / effectiveHigh) * 100;
            return new RiskAssessment(
                    RiskSignal.TRAILING_STOP,
                    String.format("트레일링 스탑 발동: 고점($%.2f) 대비 -%.1f%% 하락. 매도 검토 권장.", effectiveHigh, dropFromHigh),
                    stopLossStr,
                    trailingStopStr
            );
        }

        // 3. 재평가 신호: 평가 점수 55점 미만
        if (evaluationScore >= 0 && evaluationScore < 55) {
            return new RiskAssessment(
                    RiskSignal.REVIEW,
                    String.format("재평가 필요: 평가 점수 %.1f점 (C등급 미만). 보유 근거 재검토 권장.", evaluationScore),
                    stopLossStr,
                    trailingStopStr
            );
        }

        // 4. 정상 보유
        double gainPct = (currentPrice / purchasePrice - 1.0) * 100;
        return new RiskAssessment(
                RiskSignal.HOLD,
                String.format("보유 유지: 수익률 %+.1f%%. 손절가 $%s, 트레일링 $%s",
                        gainPct, stopLossStr, trailingStopStr),
                stopLossStr,
                trailingStopStr
        );
    }
}
