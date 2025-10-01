package com.finance.dart.api.abroad.util.sec;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 전체 제무정보 태그에서 발행주식수 태그를 찾아 최신 발행주식수 정보를 반환
 */
public class SharesOutstandingExtractor {

    /** 결과 구조 */
    public static class SharesResult {
        public final String namespace;     // 예: us-gaap / ifrs-full / dei / (custom prefix)
        public final String concept;       // 예: CommonStockSharesOutstanding
        public final String end;           // 기준일 YYYY-MM-DD
        public final String filed;         // 제출일 YYYY-MM-DD
        public final String accn;          // 제출번호
        public final double value;         // 주식 수
        public final boolean fromDimensions; // 차원값/합산 기반인지
        public final String pickedPolicy;  // 어떤 규칙으로 고른 값인지 간단 표기

        public SharesResult(String ns, String concept, String end, String filed,
                            String accn, double value, boolean fromDimensions, String pickedPolicy) {
            this.namespace = ns;
            this.concept = concept;
            this.end = end;
            this.filed = filed;
            this.accn = accn;
            this.value = value;
            this.fromDimensions = fromDimensions;
            this.pickedPolicy = pickedPolicy;
        }

        @Override public String toString() {
            return "SharesResult{" +
                    "ns='" + namespace + '\'' +
                    ", concept='" + concept + '\'' +
                    ", end='" + end + '\'' +
                    ", filed='" + filed + '\'' +
                    ", accn='" + accn + '\'' +
                    ", value=" + value +
                    ", fromDimensions=" + fromDimensions +
                    ", pickedPolicy='" + pickedPolicy + '\'' +
                    '}';
        }
    }

    /** 우선순위 네임스페이스 (그 외는 custom 취급) */
    private static final List<String> NS_ORDER = List.of("us-gaap", "ifrs-full", "dei");

    /** 표준/자주 쓰는 발행주식수 컨셉 후보 (정확 매칭 우선) */
    private static final List<String> PREFERRED_CONCEPTS = List.of(
            "CommonStockSharesOutstanding",       // us-gaap
            "EntityCommonStockSharesOutstanding", // dei (cover)
            "OrdinarySharesNumber",               // ifrs-full (의미가 넓을 수 있음)
            "NumberOfSharesOutstanding"           // IFRS/커스텀 변형 케이스
    );

    /** 포함/제외 키워드(개념명 휴리스틱) */
    private static final List<String> INCLUDE_KEYWORDS =
            List.of("Outstanding"); // 기본적으로 Outstanding 포함
    private static final List<String> PREFER_COMMON_KEYWORDS =
            List.of("CommonStock", "CommonUnit", "Ordinary"); // 보통주/일반주 우선
    private static final List<String> EXCLUDE_KEYWORDS = List.of(
            "Preferred", "TemporaryEquity", "WeightedAverage", "Diluted", "Basic",
            "Authorized", "Issued" // 권한/발행누계는 제외
    );

    /**
     * factsRoot: companyfacts JSON을 Map으로 파싱한 루트
     * sumDimensionsIfNeeded: 차원 없는 값이 없을 때 같은 날짜의 차원값을 합산할지 여부
     * preferCommonOnly: 개념명에 Common/Ordinary/Unit 류가 포함된 것만 최종 채택할지 여부
     */
    public static SharesResult extractLatestShares(Map<String, Object> factsRoot,
                                                   boolean sumDimensionsIfNeeded,
                                                   boolean preferCommonOnly) {
        Map<String, Object> facts = asMap(factsRoot.get("facts"));
        if (facts == null) return null;

        // 1) 표준 컨셉 우선 탐색: ns 우선순위별로 정확 매칭
        SharesResult best = tryPreferredConcepts(facts, sumDimensionsIfNeeded, preferCommonOnly);
        if (best != null) return best;

        // 2) 전수 스캔: 모든 ns/컨셉 중 Outstanding 포함 + exclude 제외 + (Common/Ordinary/Unit 우선)
        return tryFullScanHeuristic(facts, sumDimensionsIfNeeded, preferCommonOnly);
    }

    // -------- 1) 표준 컨셉 우선 탐색 --------
    private static SharesResult tryPreferredConcepts(Map<String, Object> facts,
                                                     boolean sumDimensionsIfNeeded,
                                                     boolean preferCommonOnly) {
        SharesResult best = null;

        // 표준 ns 우선
        for (String ns : NS_ORDER) {
            Map<String, Object> bucket = asMap(facts.get(ns));
            if (bucket == null) continue;

            for (String concept : PREFERRED_CONCEPTS) {
                Map<String, Object> node = asMap(bucket.get(concept));
                if (node == null) continue;

                SharesResult pick = pickFromConcept(ns, concept, node, sumDimensionsIfNeeded);
                if (pick == null) continue;

                if (preferCommonOnly && !isCommonLike(concept)) {
                    // common 우선인데 개념명이 common류가 아니면 보류
                    continue;
                }
                if (isBetter(pick, best)) best = pick;
            }
            if (best != null) return best; // ns 우선순위 유지
        }

        // 커스텀 ns에서도 preferred 이름을 써봤을 수 있음(거의 드묾)
        for (String ns : facts.keySet()) {
            if (NS_ORDER.contains(ns)) continue; // 이미 위에서 탐색
            Map<String, Object> bucket = asMap(facts.get(ns));
            if (bucket == null) continue;

            for (String concept : PREFERRED_CONCEPTS) {
                Map<String, Object> node = asMap(bucket.get(concept));
                if (node == null) continue;
                SharesResult pick = pickFromConcept(ns, concept, node, sumDimensionsIfNeeded);
                if (pick == null) continue;
                if (preferCommonOnly && !isCommonLike(concept)) continue;
                if (isBetter(pick, best)) best = pick;
            }
        }
        return best;
    }

    // -------- 2) 전수 스캔 휴리스틱 --------
    private static SharesResult tryFullScanHeuristic(Map<String, Object> facts,
                                                     boolean sumDimensionsIfNeeded,
                                                     boolean preferCommonOnly) {
        SharesResult bestCommon = null;
        SharesResult bestAny = null;

        // 2-1) 표준 ns 먼저 훑기
        for (String ns : NS_ORDER) {
            Map<String, Object> bucket = asMap(facts.get(ns));
            if (bucket == null) continue;
            SharesResult[] two = scanBucket(ns, bucket, sumDimensionsIfNeeded, preferCommonOnly);
            if (two[0] != null && isBetter(two[0], bestCommon)) bestCommon = two[0];
            if (two[1] != null && isBetter(two[1], bestAny))    bestAny    = two[1];
            if (bestCommon != null) return bestCommon; // common 우선
        }

        // 2-2) 커스텀(nsOrder 외) 훑기
        for (String ns : facts.keySet()) {
            if (NS_ORDER.contains(ns)) continue;
            Map<String, Object> bucket = asMap(facts.get(ns));
            if (bucket == null) continue;
            SharesResult[] two = scanBucket(ns, bucket, sumDimensionsIfNeeded, preferCommonOnly);
            if (two[0] != null && isBetter(two[0], bestCommon)) bestCommon = two[0];
            if (two[1] != null && isBetter(two[1], bestAny))    bestAny    = two[1];
        }

        return (bestCommon != null) ? bestCommon : bestAny;
    }

    /** 특정 ns 버킷을 훑어 Common-like 최선 / Any 최선 을 동시에 반환 */
    private static SharesResult[] scanBucket(String ns, Map<String, Object> bucket,
                                             boolean sumDimensionsIfNeeded,
                                             boolean preferCommonOnly) {
        SharesResult bestCommon = null;
        SharesResult bestAny = null;

        for (String concept : bucket.keySet()) {
            // 필터: 이름 휴리스틱
            if (!containsAny(concept, INCLUDE_KEYWORDS)) continue;
            if (containsAny(concept, EXCLUDE_KEYWORDS)) continue;

            Map<String, Object> node = asMap(bucket.get(concept));
            if (node == null) continue;

            SharesResult pick = pickFromConcept(ns, concept, node, sumDimensionsIfNeeded);
            if (pick == null) continue;

            if (isCommonLike(concept)) {
                if (isBetter(pick, bestCommon)) bestCommon = pick;
            } else if (!preferCommonOnly) { // common만 원할 때는 any는 아예 보류
                if (isBetter(pick, bestAny)) bestAny = pick;
            }
        }
        return new SharesResult[]{bestCommon, bestAny};
    }

    // -------- 개별 concept에서 값 고르기 --------
    private static SharesResult pickFromConcept(String ns, String concept,
                                                Map<String, Object> conceptNode,
                                                boolean sumDimensionsIfNeeded) {
        Map<String, Object> units = asMap(conceptNode.get("units"));
        if (units == null) return null;

        List<Map<String, Object>> arr = asListOfMap(units.get("shares"));
        if (arr == null || arr.isEmpty()) return null;

        List<Map<String, Object>> noDim = new ArrayList<>();
        List<Map<String, Object>> withDim = new ArrayList<>();
        for (Map<String, Object> x : arr) {
            if (hasDim(x)) withDim.add(x);
            else noDim.add(x);
        }

        if (!noDim.isEmpty()) {
            noDim.sort((a, b) -> pickDate(b).compareTo(pickDate(a)));
            Map<String, Object> top = noDim.get(0);
            return toResult(ns, concept, top, false, "no-dimension-latest");
        }

        if (sumDimensionsIfNeeded && !withDim.isEmpty()) {
            // 같은 날짜 end(없으면 filed)별 합산 → 최신일자 선택
            Map<String, List<Map<String, Object>>> byDate = withDim.stream()
                    .collect(Collectors.groupingBy(SharesOutstandingExtractor::pickDate));
            String latest = byDate.keySet().stream()
                    .sorted((a, b) -> b.compareTo(a))
                    .findFirst().orElse(null);
            if (latest != null) {
                List<Map<String, Object>> rows = byDate.get(latest);
                double sum = rows.stream().mapToDouble(r -> num(r, "val")).sum();
                Map<String, Object> rep = rows.get(0);
                return new SharesResult(
                        ns, concept, str(rep, "end"), str(rep, "filed"), str(rep, "accn"),
                        sum, true, "dimension-sum@" + latest
                );
            }
        }
        return null;
    }

    // -------- 유틸/헬퍼 --------

    private static boolean isCommonLike(String name) {
        String s = name.toLowerCase();
        for (String k : PREFER_COMMON_KEYWORDS) {
            if (s.contains(k.toLowerCase())) return true;
        }
        return false;
    }

    private static boolean containsAny(String name, List<String> ks) {
        String s = name.toLowerCase();
        for (String k : ks) {
            if (s.contains(k.toLowerCase())) return true;
        }
        return false;
    }

    private static boolean hasDim(Map<String, Object> n) {
        return n.containsKey("segments") || n.containsKey("dim");
    }

    /** end 우선, 없으면 filed */
    private static String pickDate(Map<String, Object> m) {
        String end = str(m, "end");
        String filed = str(m, "filed");
        return (end != null && !end.isEmpty()) ? end : (filed != null ? filed : "");
    }

    private static SharesResult toResult(String ns, String concept, Map<String, Object> n,
                                         boolean fromDim, String policy) {
        return new SharesResult(
                ns, concept,
                str(n, "end"), str(n, "filed"), str(n, "accn"),
                num(n, "val"), fromDim, policy
        );
    }

    private static boolean isBetter(SharesResult a, SharesResult b) {
        if (b == null) return true;
        String da = (a.end != null && !a.end.isEmpty()) ? a.end : (a.filed != null ? a.filed : "");
        String db = (b.end != null && !b.end.isEmpty()) ? b.end : (b.filed != null ? b.filed : "");
        return da.compareTo(db) > 0;
    }

    private static Map<String, Object> asMap(Object o) {
        return (o instanceof Map) ? (Map<String, Object>) o : null;
    }

    private static List<Map<String, Object>> asListOfMap(Object o) {
        if (!(o instanceof List)) return null;
        List<?> raw = (List<?>) o;
        List<Map<String, Object>> res = new ArrayList<>();
        for (Object e : raw) {
            if (e instanceof Map) res.add((Map<String, Object>) e);
        }
        return res;
    }

    private static String str(Map<String, Object> m, String key) {
        Object v = m.get(key);
        return v == null ? null : String.valueOf(v);
    }

    private static double num(Map<String, Object> m, String key) {
        Object v = m.get(key);
        if (v == null) return 0d;
        if (v instanceof Number) return ((Number) v).doubleValue();
        try { return Double.parseDouble(String.valueOf(v)); }
        catch (Exception ignored) { return 0d; }
    }

}
