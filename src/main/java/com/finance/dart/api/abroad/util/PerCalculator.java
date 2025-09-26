package com.finance.dart.api.abroad.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class PerCalculator {

    /**
     * PER 계산
     * @param facts SEC companyfacts JSON Map
     * @param price 현재 주가
     * @return PER 값 (Double), 없으면 null
     */
    public static Double calculatePER(Map<String, Object> facts, double price) {
        if (facts == null || facts.isEmpty() || price <= 0) return null;
        Double eps = extractLatestAnnualEPS(facts);
        if (eps == null || eps <= 0) return null;
        return price / eps;
    }

    /**
     * 가장 최신 연간 EPS 추출
     */
    private static Double extractLatestAnnualEPS(Map<String, Object> facts) {
        try {
            Map<String, Object> factMap = (Map<String, Object>) facts.get("facts");
            if (factMap == null) return null;

            // EPS 우선순위: 희석 → 기본
            String[] concepts = {"EarningsPerShareDiluted", "EarningsPerShareBasic"};

            for (String concept : concepts) {
                Map<String, Object> gaap = (Map<String, Object>) factMap.get("us-gaap");
                if (gaap == null) continue;

                Map<String, Object> conceptData = (Map<String, Object>) gaap.get(concept);
                if (conceptData == null) continue;

                Map<String, Object> units = (Map<String, Object>) conceptData.get("units");
                if (units == null || units.isEmpty()) continue;

                // ✅ 모든 unit 배열을 한데 모아 탐색 ("USD/shares", "USD/share", "USD")
                List<Map<String, Object>> all = new ArrayList<>();
                for (Object v : units.values()) {
                    if (v instanceof List) {
                        all.addAll((List<Map<String, Object>>) v);
                    }
                }
                if (all.isEmpty()) continue;

                // 최신 end 기준 정렬
                all.sort((a, b) -> {
                    String endA = (String) a.get("end");
                    String endB = (String) b.get("end");
                    if (endA == null && endB == null) return 0;
                    if (endA == null) return 1;
                    if (endB == null) return -1;
                    return endB.compareTo(endA);
                });

                // 1) FY 또는 CY연도 프레임만 먼저 찾기
                for (Map<String, Object> item : all) {
                    String fp = s(item.get("fp"));
                    String frame = s(item.get("frame"));
                    if ("FY".equalsIgnoreCase(fp) || (frame != null && frame.matches("CY\\d{4}"))) {
                        Double val = toDouble(item.get("val"));
                        if (val != null) return val;
                    }
                }
                // 2) 없으면 최신 아무 값이라도 반환 (fallback)
                for (Map<String, Object> item : all) {
                    Double val = toDouble(item.get("val"));
                    if (val != null) return val;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String s(Object o) {
        return o == null ? null : String.valueOf(o);
    }

    private static Double toDouble(Object o) {
        if (o == null) return null;
        try {
            return Double.valueOf(String.valueOf(o));
        } catch (Exception ignored) {
            return null;
        }
    }
}