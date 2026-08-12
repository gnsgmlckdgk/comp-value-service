package com.finance.dart.api.common.service;

import com.finance.dart.api.common.dto.evaluation.PerformanceSummaryDto;
import com.finance.dart.api.common.entity.EvaluationHistoryEntity;
import com.finance.dart.api.common.repository.EvaluationHistoryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

/**
 * 수익률 추적 서비스 (2-4)
 * - 야간 평가가 매일 저장한 current_price만으로 수익률 계산 (추가 FMP 호출 없음)
 * - "특정 기준일의 투자판정/가치등급별로 이후 실제 수익률이 어땠는가" 집계 → 신뢰도 데이터 검증
 */
@Slf4j
@Service
@AllArgsConstructor
public class EvaluationPerformanceService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final EvaluationHistoryRepository evaluationHistoryRepository;

    /**
     * 기준일 대비 최신일까지의 수익률을 투자판정·가치등급별로 집계
     * @param fromDate 기준일 (해당일에 평가된 종목들의 이후 성과)
     * @return {"bySignal": [...], "byGrade": [...], "baseDate":..., "latestDate":...}
     */
    @Transactional(readOnly = true)
    public Map<String, Object> computePerformance(LocalDate fromDate) {
        Map<String, Object> result = new LinkedHashMap<>();

        List<EvaluationHistoryEntity> baseline = evaluationHistoryRepository.findBySnapshotDate(fromDate);
        LocalDate latestDate = evaluationHistoryRepository.findFirstByOrderBySnapshotDateDesc()
                .map(EvaluationHistoryEntity::getSnapshotDate)
                .orElse(null);

        result.put("baseDate", fromDate != null ? fromDate.toString() : null);
        result.put("latestDate", latestDate != null ? latestDate.toString() : null);

        if (baseline.isEmpty() || latestDate == null || latestDate.equals(fromDate)) {
            // 데이터 부족(기준일=최신일이면 경과기간 0) → 빈 집계
            result.put("bySignal", List.of());
            result.put("byGrade", List.of());
            result.put("note", "수익률 산출에는 기준일 이후 경과한 스냅샷이 필요합니다.");
            return result;
        }

        Map<String, BigDecimal> latestPrices = new HashMap<>();
        for (EvaluationHistoryEntity e : evaluationHistoryRepository.findBySnapshotDate(latestDate)) {
            if (e.getCurrentPrice() != null) latestPrices.put(e.getSymbol(), e.getCurrentPrice());
        }

        result.put("bySignal", computeSummaries(baseline, latestPrices, EvaluationHistoryEntity::getInvestmentSignal));
        result.put("byGrade", computeSummaries(baseline, latestPrices, EvaluationHistoryEntity::getValueGrade));
        return result;
    }

    /**
     * 그룹별 수익률 집계 (순수 함수 - 단위 테스트 대상)
     * @param baseline 기준일 평가 종목들 (currentPrice = 기준가)
     * @param latestPrices 최신일 종목별 가격
     * @param keyFn 그룹 키 추출 (투자판정 or 가치등급)
     */
    static List<PerformanceSummaryDto> computeSummaries(
            List<EvaluationHistoryEntity> baseline,
            Map<String, BigDecimal> latestPrices,
            Function<EvaluationHistoryEntity, String> keyFn) {

        // 그룹 → 수익률 리스트
        Map<String, List<Double>> byGroup = new LinkedHashMap<>();
        for (EvaluationHistoryEntity e : baseline) {
            BigDecimal basePrice = e.getCurrentPrice();
            BigDecimal latestPrice = latestPrices.get(e.getSymbol());
            if (basePrice == null || latestPrice == null
                    || basePrice.compareTo(BigDecimal.ZERO) <= 0) {
                continue;  // 가격 없거나 기준가 0 이하 → 제외
            }
            String key = keyFn.apply(e);
            if (key == null) key = "(미분류)";

            double returnPct = latestPrice.subtract(basePrice)
                    .divide(basePrice, 6, java.math.RoundingMode.HALF_UP)
                    .multiply(HUNDRED)
                    .doubleValue();
            byGroup.computeIfAbsent(key, k -> new ArrayList<>()).add(returnPct);
        }

        List<PerformanceSummaryDto> summaries = new ArrayList<>();
        for (Map.Entry<String, List<Double>> entry : byGroup.entrySet()) {
            List<Double> returns = entry.getValue();
            int n = returns.size();
            double sum = 0, max = Double.NEGATIVE_INFINITY, min = Double.POSITIVE_INFINITY;
            int wins = 0;
            for (double r : returns) {
                sum += r;
                if (r > max) max = r;
                if (r < min) min = r;
                if (r > 0) wins++;
            }
            summaries.add(PerformanceSummaryDto.builder()
                    .group(entry.getKey())
                    .count(n)
                    .avgReturnPct(round2(sum / n))
                    .winRatePct(round2(wins * 100.0 / n))
                    .maxReturnPct(round2(max))
                    .minReturnPct(round2(min))
                    .build());
        }
        return summaries;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
