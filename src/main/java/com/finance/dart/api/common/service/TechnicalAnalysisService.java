package com.finance.dart.api.common.service;

import com.finance.dart.api.abroad.dto.fmp.chart.StockPriceVolumeResDto;
import com.finance.dart.api.abroad.dto.fmp.quote.StockQuoteResDto;
import com.finance.dart.api.common.dto.CompanySharePriceResultDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * V8: 모멘텀/기술적 분석 서비스
 * - 이동평균선(SMA50/200) 분석
 * - RSI 14일
 * - 거래량 추세
 * - 하드 게이트 (데스크로스 + 약세 등)
 */
@Slf4j
@AllArgsConstructor
@Service
public class TechnicalAnalysisService {

    /**
     * 기술적 분석 결과
     */
    @Getter
    public static class TechnicalAnalysisResult {
        private final int maScore;          // 0~6
        private final int rsiScore;         // 0~5
        private final int volumeScore;      // 0~4
        private final boolean gatePass;     // 하드 게이트 통과 여부
        private final String gateReason;    // 게이트 실패 사유

        public TechnicalAnalysisResult(int maScore, int rsiScore, int volumeScore,
                                       boolean gatePass, String gateReason) {
            this.maScore = maScore;
            this.rsiScore = rsiScore;
            this.volumeScore = volumeScore;
            this.gatePass = gatePass;
            this.gateReason = gateReason;
        }

        public int getTotalScore() {
            return maScore + rsiScore + volumeScore;
        }
    }

    /**
     * 기술적 분석 수행
     * @param stockQuote StockQuote 데이터 (SMA50, SMA200, price)
     * @param priceHistory 52주 일별 가격 히스토리 (RSI, 거래량용)
     * @param resultDetail 분석 결과를 기록할 DTO
     * @return 분석 결과
     */
    public TechnicalAnalysisResult analyze(StockQuoteResDto stockQuote,
                                            List<StockPriceVolumeResDto> priceHistory,
                                            CompanySharePriceResultDetail resultDetail) {

        int maScore = 0;
        int rsiScore = 0;
        int volumeScore = 3;  // 데이터 없으면 중립
        boolean gatePass = true;
        String gateReason = "";

        // 1. 이동평균선 분석
        if (stockQuote != null && stockQuote.getPrice() != null
                && stockQuote.getPriceAvg50() != null && stockQuote.getPriceAvg200() != null) {

            double price = stockQuote.getPrice();
            double sma50 = stockQuote.getPriceAvg50();
            double sma200 = stockQuote.getPriceAvg200();

            if (price > sma50 && sma50 > sma200) {
                // 강세 배열: price > SMA50 > SMA200
                maScore = 6;
            } else if (price > sma50 || price > sma200) {
                // 일부 강세
                maScore = 3;
            } else {
                // 약세 배열
                maScore = 0;
            }

            // 하드 게이트: 데스크로스(SMA50 < SMA200) AND 주가 < SMA200
            if (sma50 < sma200 && price < sma200) {
                gatePass = false;
                gateReason = "데스크로스(SMA50<SMA200) + 주가<SMA200";
            }

            resultDetail.setSMA50(String.format("%.2f", sma50));
            resultDetail.setSMA200(String.format("%.2f", sma200));
        }

        // 2. RSI 14일 계산 + 3. 거래량 추세
        if (priceHistory != null && priceHistory.size() >= 20) {
            // RSI 계산 (최근 14일 close 기반)
            double rsi = calculateRSI(priceHistory, 14);
            resultDetail.setRSI(String.format("%.1f", rsi));

            if (rsi >= 40 && rsi <= 60) {
                rsiScore = 5;  // 건강한 중립
            } else if (rsi >= 30 && rsi < 40) {
                rsiScore = 4;  // 과매도 접근
            } else if (rsi > 60 && rsi <= 70) {
                rsiScore = 3;  // 약간 과매수
            } else if (rsi < 30) {
                rsiScore = 1;  // 극단 과매도 (위험)
            } else {
                rsiScore = 0;  // >70 과매수
            }

            // 하드 게이트: RSI < 20 AND 주가 < SMA50
            if (rsi < 20 && stockQuote != null && stockQuote.getPrice() != null
                    && stockQuote.getPriceAvg50() != null
                    && stockQuote.getPrice() < stockQuote.getPriceAvg50()) {
                gatePass = false;
                gateReason = gateReason.isEmpty() ? "RSI<20 + 주가<SMA50" : gateReason + " / RSI<20 + 주가<SMA50";
            }

            // 거래량 추세 (최근 10일 평균 / 이전 10일 평균)
            double volumeRatio = calculateVolumeRatio(priceHistory);
            if (volumeRatio > 0) {
                resultDetail.set거래량비율(String.format("%.2f", volumeRatio));

                if (volumeRatio < 0.8) {
                    volumeScore = 4;  // 매도 압력 감소
                } else if (volumeRatio <= 1.0) {
                    volumeScore = 3;
                } else if (volumeRatio <= 1.2) {
                    volumeScore = 2;  // 중립
                } else {
                    volumeScore = 0;  // 매도 압력 증가
                }
            }
        }

        // 결과를 resultDetail에 기록
        resultDetail.set모멘텀_MA점수(maScore);
        resultDetail.set모멘텀_RSI점수(rsiScore);
        resultDetail.set모멘텀_거래량점수(volumeScore);
        resultDetail.set모멘텀게이트통과(gatePass);
        resultDetail.set모멘텀게이트사유(gatePass ? "통과" : gateReason);

        return new TechnicalAnalysisResult(maScore, rsiScore, volumeScore, gatePass, gateReason);
    }

    /**
     * RSI 14일 계산
     * @param priceHistory 일별 가격 (최신 → 과거 순서)
     * @param period RSI 기간 (일반적으로 14)
     * @return RSI 값 (0~100)
     */
    private double calculateRSI(List<StockPriceVolumeResDto> priceHistory, int period) {
        if (priceHistory.size() < period + 1) return 50.0;  // 데이터 부족 시 중립

        double avgGain = 0;
        double avgLoss = 0;

        // 첫 번째 평균 계산 (최신 데이터가 index 0)
        for (int i = 0; i < period; i++) {
            Double current = priceHistory.get(i).getClose();
            Double previous = priceHistory.get(i + 1).getClose();
            if (current == null || previous == null) continue;

            double change = current - previous;
            if (change > 0) {
                avgGain += change;
            } else {
                avgLoss += Math.abs(change);
            }
        }

        avgGain /= period;
        avgLoss /= period;

        if (avgLoss == 0) return 100.0;

        double rs = avgGain / avgLoss;
        return 100.0 - (100.0 / (1.0 + rs));
    }

    /**
     * 거래량 비율 계산 (최근 10일 평균 / 이전 10일 평균)
     * @param priceHistory 일별 가격 (최신 → 과거 순서)
     * @return 거래량 비율 (> 1.0이면 거래량 증가)
     */
    private double calculateVolumeRatio(List<StockPriceVolumeResDto> priceHistory) {
        if (priceHistory.size() < 20) return -1;

        double recentSum = 0;
        double olderSum = 0;
        int recentCount = 0;
        int olderCount = 0;

        for (int i = 0; i < 10; i++) {
            Long vol = priceHistory.get(i).getVolume();
            if (vol != null && vol > 0) {
                recentSum += vol;
                recentCount++;
            }
        }

        for (int i = 10; i < 20; i++) {
            Long vol = priceHistory.get(i).getVolume();
            if (vol != null && vol > 0) {
                olderSum += vol;
                olderCount++;
            }
        }

        if (recentCount == 0 || olderCount == 0) return -1;

        double recentAvg = recentSum / recentCount;
        double olderAvg = olderSum / olderCount;

        if (olderAvg == 0) return -1;

        return recentAvg / olderAvg;
    }
}
