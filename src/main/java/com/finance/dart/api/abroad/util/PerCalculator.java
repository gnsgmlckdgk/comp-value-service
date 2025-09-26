package com.finance.dart.api.abroad.util;

import java.time.LocalDate;
import java.util.*;

@SuppressWarnings("unchecked")
public class PerCalculator {

    /**
     * PER 계산 (연간 EPS 기반)
     * - EPS 우선순위: Diluted → Basic
     * - taxonomy 우선순위: us-gaap → ifrs-full (FPI 대비)
     * - 단위: {*}/shares 만 허용 (예: "USD/shares")
     * - 기간: FY 또는 frame = CYyyyy 만 사용
     * - 최신성: filed 날짜 최신 우선 → end 날짜 보조
     * - EPS <= 0 이면 PER null 반환
     *
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

    /** 가장 최신 연간 EPS 추출 (us-gaap → ifrs-full, Diluted → Basic) */
    private static Double extractLatestAnnualEPS(Map<String, Object> facts) {
        Map<String, Object> factMap = (Map<String, Object>) facts.get("facts");
        if (factMap == null) return null;

        String[] taxOrder = {"us-gaap", "ifrs-full"};
        String[] concepts = {"EarningsPerShareDiluted", "EarningsPerShareBasic"};

        EpsPick best = null;
        for (String tax : taxOrder) {
            Map<String, Object> taxMap = (Map<String, Object>) factMap.get(tax);
            if (taxMap == null) continue;

            for (String concept : concepts) {
                Map<String, Object> conceptData = (Map<String, Object>) taxMap.get(concept);
                if (conceptData == null) continue;

                Map<String, Object> units = (Map<String, Object>) conceptData.get("units");
                if (units == null || units.isEmpty()) continue;

                List<Map<String, Object>> all = collectShareBasedUnitRows(units);
                if (all.isEmpty()) continue;

                // FY/Calendar 연도만 필터
                List<EpsRow> annualRows = new ArrayList<>();
                for (Map<String, Object> item : all) {
                    if (!isAnnual(item)) continue;
                    Double v = toDouble(item.get("val"));
                    if (v == null) continue;
                    LocalDate filed = toDate(s(item.get("filed")));
                    LocalDate end = toDate(s(item.get("end")));
                    Integer fy = toInt(item.get("fy"));
                    annualRows.add(new EpsRow(v, filed, end, fy, tax, concept));
                }

                if (!annualRows.isEmpty()) {
                    annualRows.sort(EpsRow::compareByRecencyDesc);
                    EpsPick candidate = annualRows.get(0).toPick();
                    if (isBetter(candidate, best)) best = candidate;
                }

                // us-gaap에서 이미 찾았으면 더 진행할 필요 없음 (우선순위 상)
                if (best != null && "us-gaap".equals(best.taxonomy)) return best.val;
            }
        }
        return best == null ? null : best.val;
    }

    /** units에서 키가 "/shares"로 끝나는 배열만 모아 하나의 리스트로 병합 */
    private static List<Map<String, Object>> collectShareBasedUnitRows(Map<String, Object> units) {
        List<Map<String, Object>> all = new ArrayList<>();
        for (Map.Entry<String, Object> e : units.entrySet()) {
            String unitKey = e.getKey();
            if (unitKey == null || !unitKey.endsWith("/shares")) continue;
            Object v = e.getValue();
            if (v instanceof List) {
                try { all.addAll((List<Map<String, Object>>) v); } catch (Exception ignored) {}
            }
        }
        return all;
    }

    /** FY 또는 frame = CYyyyy 이면 연간으로 간주 */
    private static boolean isAnnual(Map<String, Object> item) {
        String fp = s(item.get("fp"));
        String frame = s(item.get("frame"));
        if ("FY".equalsIgnoreCase(fp)) return true;
        return frame != null && frame.matches("^CY\\d{4}$");
    }

    // ---- helpers ----
    private static String s(Object o) { return o == null ? null : String.valueOf(o); }
    private static Integer toInt(Object o) { try { return o == null ? null : Integer.valueOf(String.valueOf(o)); } catch (Exception e) { return null; } }
    private static Double toDouble(Object o) { if (o == null) return null; try { return Double.valueOf(String.valueOf(o)); } catch (Exception e) { return null; } }
    private static LocalDate toDate(String s) { try { return s == null ? null : LocalDate.parse(s); } catch (Exception e) { return null; } }

    // ---- model & ordering ----
    private static class EpsRow {
        final Double val; final LocalDate filed; final LocalDate end; final Integer fy; final String tax; final String tag;
        EpsRow(Double val, LocalDate filed, LocalDate end, Integer fy, String tax, String tag) {
            this.val = val; this.filed = filed; this.end = end; this.fy = fy; this.tax = tax; this.tag = tag;
        }
        static int compareByRecencyDesc(EpsRow a, EpsRow b) {
            // filed 최신 우선 → end 최신 → fy 큰 값
            int c1 = compareDesc(a.filed, b.filed);
            if (c1 != 0) return c1;
            int c2 = compareDesc(a.end, b.end);
            if (c2 != 0) return c2;
            return Integer.compare(b.fy == null ? Integer.MIN_VALUE : b.fy,
                                   a.fy == null ? Integer.MIN_VALUE : a.fy);
        }
        static <T extends Comparable<T>> int compareDesc(T a, T b) {
            if (a == null && b == null) return 0;
            if (a == null) return 1;
            if (b == null) return -1;
            return b.compareTo(a);
        }
        EpsPick toPick() { return new EpsPick(val, fy, tax); }
    }

    private static class EpsPick {
        final Double val; final Integer fy; final String taxonomy;
        EpsPick(Double val, Integer fy, String taxonomy) { this.val = val; this.fy = fy; this.taxonomy = taxonomy; }
    }

    private static boolean isBetter(EpsPick cand, EpsPick cur) {
        if (cur == null) return true;
        // taxonomy 우선순위: us-gaap가 있으면 그대로 유지, 없으면 교체 허용
        if ("us-gaap".equals(cur.taxonomy) && !"us-gaap".equals(cand.taxonomy)) return false;
        if (!"us-gaap".equals(cur.taxonomy) && "us-gaap".equals(cand.taxonomy)) return true;
        // 동일 taxonomy면 최근 연도 우선
        int fyCur = cur.fy == null ? Integer.MIN_VALUE : cur.fy;
        int fyNew = cand.fy == null ? Integer.MIN_VALUE : cand.fy;
        return fyNew >= fyCur;
    }
}