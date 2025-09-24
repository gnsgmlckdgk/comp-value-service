package com.finance.dart.api.abroad.util;

import com.finance.dart.api.abroad.dto.financial.statement.USD;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 영업이익 추출 유틸
 * 우선순위:
 *  1) us-gaap:OperatingIncomeLoss
 *  2) 커스텀(네임스페이스 != us-gaap) 중 'OperatingIncome|OperatingProfit' 포함
 *  3) 근사1: PreTax + InterestExpense
 *  4) 근사2: GrossProfit - SG&A - R&D - OtherOperatingExpense
 *
 *  - 분기 우선 → 없으면 연간 폴백
 *  - 동일 기간 매칭은 (start,end,fy,fp)의 문자열 키를 사용
 */
public class OperatingIncomeExtractor {

    // ====== 공개 API ======

    /**
     * @param facts        SEC companyfacts(Map 파싱 결과)에서 필요한 리스트를 꺼내는 어댑터 함수를 함께 사용
     * @param preferQuarter 분기 먼저 시도 (true 권장)
     * @return Result(value, conceptUsed, method, usdMeta 등)
     */
    public static Result extractLatestOperatingIncome(Map<String, Object> facts, boolean preferQuarter) {
        // 1) 표준 태그
        Result r = pickLatestByConcept(facts, "us-gaap", "OperatingIncomeLoss", preferQuarter, Method.DIRECT_USGAAP);
        if (r != null) return r;

        // 2) 커스텀 탐색 (네임스페이스 전체 검색)
        r = pickLatestByCustomOperating(facts, preferQuarter);
        if (r != null) return r;

        // 3) 근사: PreTax + InterestExpense
        r = approxByPretaxPlusInterest(facts, preferQuarter);
        if (r != null) return r;

        // 4) 근사: GrossProfit - SG&A - R&D - OtherOperatingExpense
        r = approxByComponents(facts, preferQuarter);
        if (r != null) return r;

        return null; // 정말로 못 찾았을 때
    }

    /**
     * Extract multiple recent operating income results up to the specified limit.
     * Uses tiered fallback: us-gaap → custom → approx preTax+Interest → approx components.
     * @param facts SEC companyfacts map
     * @param preferQuarter prefer quarter if true, else annual
     * @param limit max number of results to return
     * @return list of Result objects sorted by filed date descending
     */
    public static List<Result> extractRecentOperatingIncomes(Map<String, Object> facts, boolean preferQuarter, int limit) {
        List<Result> results = new ArrayList<>();

        List<Result> usGaapList = seriesByUsGaap(facts, preferQuarter, limit);
        results.addAll(usGaapList);
        if (results.size() >= limit) return results.stream()
                .sorted((a,b) -> compareDate(coalesce(b.meta.getFiled(), b.meta.getEnd()), coalesce(a.meta.getFiled(), a.meta.getEnd())))
                .limit(limit)
                .collect(Collectors.toList());

        List<Result> customList = seriesByCustomOperating(facts, preferQuarter, limit);
        for (Result r : customList) {
            if (results.size() >= limit) break;
            if (results.stream().noneMatch(existing -> periodKey(existing.meta).equals(periodKey(r.meta)))) {
                results.add(r);
            }
        }
        if (results.size() >= limit) return results.stream()
                .sorted((a,b) -> compareDate(coalesce(b.meta.getFiled(), b.meta.getEnd()), coalesce(a.meta.getFiled(), a.meta.getEnd())))
                .limit(limit)
                .collect(Collectors.toList());

        List<Result> approx1List = seriesByPretaxPlusInterest(facts, preferQuarter, limit);
        for (Result r : approx1List) {
            if (results.size() >= limit) break;
            if (results.stream().noneMatch(existing -> periodKey(existing.meta).equals(periodKey(r.meta)))) {
                results.add(r);
            }
        }
        if (results.size() >= limit) return results.stream()
                .sorted((a,b) -> compareDate(coalesce(b.meta.getFiled(), b.meta.getEnd()), coalesce(a.meta.getFiled(), a.meta.getEnd())))
                .limit(limit)
                .collect(Collectors.toList());

        List<Result> approx2List = seriesByComponents(facts, preferQuarter, limit);
        for (Result r : approx2List) {
            if (results.size() >= limit) break;
            if (results.stream().noneMatch(existing -> periodKey(existing.meta).equals(periodKey(r.meta)))) {
                results.add(r);
            }
        }

        return results.stream()
                .sorted((a,b) -> compareDate(coalesce(b.meta.getFiled(), b.meta.getEnd()), coalesce(a.meta.getFiled(), a.meta.getEnd())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Extract up to 3 recent operating income results.
     * @param facts SEC companyfacts map
     * @param preferQuarter prefer quarter if true
     * @return list of up to 3 Result objects
     */
    public static List<Result> extractTriple(Map<String, Object> facts, boolean preferQuarter) {
        return extractRecentOperatingIncomes(facts, preferQuarter, 3);
    }

    // ====== 내부 구현 ======

    // A. 표준/커스텀 컨셉에서 바로 선택
    private static Result pickLatestByConcept(Map<String, Object> facts, String ns, String concept,
                                              boolean preferQuarter, Method method) {
        List<USD> list = FactAdapter.getUsdList(facts, ns, concept);
        if (list == null || list.isEmpty()) return null;

        USD picked = pickLatest(list, preferQuarter);
        if (picked == null) return null;

        return Result.of(picked.getVal(), ns + ":" + concept, method, picked);
    }

    private static Result pickLatestByCustomOperating(Map<String, Object> facts, boolean preferQuarter) {
        // 네임스페이스 전부 순회 (us-gaap 제외)
        Map<String, Set<String>> conceptsByNs = FactAdapter.listAllConceptsByNamespace(facts);
        for (var e : conceptsByNs.entrySet()) {
            String ns = e.getKey();
            if (ns == null || ns.equalsIgnoreCase("us-gaap")) continue;

            for (String concept : e.getValue()) {
                String lc = concept.toLowerCase(Locale.ROOT);
                // 영업이익 후보(부정어 필터링)
                if ((lc.contains("operatingincome") || lc.contains("operatingprofit"))
                        && !lc.contains("nonoperating")) {
                    Result r = pickLatestByConcept(facts, ns, concept, preferQuarter, Method.CUSTOM);
                    if (r != null) return r;
                }
            }
        }
        return null;
    }

    // B. 근사 1: PreTax + InterestExpense ≈ Operating Income
    private static Result approxByPretaxPlusInterest(Map<String, Object> facts, boolean preferQuarter) {
        // Pre-Tax 후보
        List<String> pretaxCandidates = List.of(
                "IncomeLossFromContinuingOperationsBeforeIncomeTaxesExtraordinaryItemsNoncontrollingInterest",
                "IncomeLossFromContinuingOperationsBeforeIncomeTaxes"
        );
        // 이자비용 후보
        List<String> interestExpenseCandidates = List.of(
                "InterestExpense",
                "InterestAndDebtExpense",
                "OperatingIncomeLossAndInterestExpense" // 일부 회사 커스텀 대응용 (무시될 수 있음)
        );

        // 각각 최신 리스트를 뽑고, 같은 기간으로 align
        USD preTax = pickFirstAvailableUsGaap(facts, pretaxCandidates, preferQuarter);
        USD interestExp = pickFirstAvailableUsGaapAligned(facts, interestExpenseCandidates, preferQuarter, preTax);

        if (preTax == null) return null;
        double v = toDouble(preTax.getVal());

        StringBuilder src = new StringBuilder("us-gaap:" + preTaxConceptUsed);
        Method method = Method.APPROX_PRETAX_INTEREST;

        if (interestExp != null) {
            v += toDouble(interestExp.getVal());
            src.append(" + ").append("us-gaap:").append(interestConceptUsed);
        } else {
            // 이자비용이 없으면 근사 품질 낮음 → 반환은 하되, steps에 메모
        }
        Result r = Result.of(v, src.toString(), method, preTax);
        r.steps.add("Approx: Operating ≈ PreTax + InterestExpense (동일 기간 정렬)");
        return r;
    }

    // C. 근사 2: GrossProfit - SG&A - R&D - OtherOperatingExpense
    private static Result approxByComponents(Map<String, Object> facts, boolean preferQuarter) {
        USD gross = pickFirstUsGaap(facts, "GrossProfit", preferQuarter);
        if (gross == null) return null;

        // 같은 기간으로 정렬할 대상들
        USD sga = pickUsGaapAligned(facts, "SellingGeneralAndAdministrativeExpense", preferQuarter, gross);
        USD rAndD = pickUsGaapAligned(facts, "ResearchAndDevelopmentExpense", preferQuarter, gross);

        // 기타영업손익(표준 후보 몇 개)
        List<String> otherOpCandidates = List.of(
                "OtherOperatingIncomeExpenseNet",
                "RestructuringCharges",
                "ImpairmentOfLongLivedAssets",
                "ImpairmentOfIntangibleAssets"
        );
        double otherOpSum = 0.0;
        for (String c : otherOpCandidates) {
            USD x = pickUsGaapAligned(facts, c, preferQuarter, gross);
            if (x != null) otherOpSum += toDouble(x.getVal());
        }

        double v = toDouble(gross.getVal())
                - (sga != null ? toDouble(sga.getVal()) : 0.0)
                - (rAndD != null ? toDouble(rAndD.getVal()) : 0.0)
                - otherOpSum;

        Result r = Result.of(v, "Constructed(Gross - SG&A - R&D - OtherOp)", Method.APPROX_COMPONENTS, gross);
        r.steps.add("Align by period with GrossProfit");
        return r;
    }

    // ====== 선택/정렬 유틸 ======

    private static USD pickLatest(List<USD> list, boolean preferQuarter) {
        if (list == null || list.isEmpty()) return null;
        Predicate<USD> isQ = Periods::isQuarter;
        Predicate<USD> isA = Periods::isAnnual;

        Comparator<USD> byFiledThenEndDesc = (a, b) -> {
            String db = coalesce(b.getFiled(), b.getEnd());
            String da = coalesce(a.getFiled(), a.getEnd());
            return compareDate(db, da); // 최신순
        };

        if (preferQuarter) {
            Optional<USD> q = list.stream().filter(isQ).sorted(byFiledThenEndDesc).findFirst();
            if (q.isPresent()) return q.get();
            return list.stream().filter(isA).sorted(byFiledThenEndDesc).findFirst().orElse(null);
        } else {
            Optional<USD> a = list.stream().filter(isA).sorted(byFiledThenEndDesc).findFirst();
            if (a.isPresent()) return a.get();
            return list.stream().filter(isQ).sorted(byFiledThenEndDesc).findFirst().orElse(null);
        }
    }

    private static USD pickFirstUsGaap(Map<String, Object> facts, String concept, boolean preferQuarter) {
        List<USD> l = FactAdapter.getUsdList(facts, "us-gaap", concept);
        return pickLatest(l, preferQuarter);
    }

    private static USD pickUsGaapAligned(Map<String, Object> facts, String concept,
                                         boolean preferQuarter, USD anchor) {
        if (anchor == null) return null;
        List<USD> l = FactAdapter.getUsdList(facts, "us-gaap", concept);
        if (l == null || l.isEmpty()) return null;

        String key = periodKey(anchor);
        return l.stream()
                .filter(u -> periodKey(u).equals(key))
                .findFirst()
                .orElseGet(() -> pickLatest(l, preferQuarter));
    }

    private static String preTaxConceptUsed;
    private static USD pickFirstAvailableUsGaap(Map<String, Object> facts, List<String> concepts, boolean preferQuarter) {
        for (String c : concepts) {
            USD x = pickFirstUsGaap(facts, c, preferQuarter);
            if (x != null) { preTaxConceptUsed = c; return x; }
        }
        return null;
    }

    private static String interestConceptUsed;
    private static USD pickFirstAvailableUsGaapAligned(Map<String, Object> facts, List<String> concepts,
                                                       boolean preferQuarter, USD anchor) {
        for (String c : concepts) {
            USD x = pickUsGaapAligned(facts, c, preferQuarter, anchor);
            if (x != null) { interestConceptUsed = c; return x; }
        }
        return null;
    }

    // 기간 정렬 키
    private static String periodKey(USD u) {
        return (coalesce(u.getStart(), "") + "|" + coalesce(u.getEnd(), "") + "|" +
                coalesce(u.getFy(), "") + "|" + coalesce(u.getFp(), "")).trim();
    }

    private static String coalesce(Object s, Object def) {
        if (s == null) return String.valueOf(def);
        String str = String.valueOf(s);
        return str.isBlank() ? String.valueOf(def) : str;
    }

    // filed/end 문자열 yyyy-MM-dd 비교 (desc)
    private static int compareDate(String d1, String d2) {
        if (d1 == null && d2 == null) return 0;
        if (d1 == null) return -1;   // null은 가장 오래된 것으로 처리
        if (d2 == null) return 1;
        LocalDate a = LocalDate.parse(d1);
        LocalDate b = LocalDate.parse(d2);
        return a.compareTo(b);       // 오름차순 (호출부에서 compareDate(db, da)로 최신이 앞으로 옴)
    }

    private static double toDouble(Long v) { return v == null ? 0.0 : v.doubleValue(); }

    // ====== 결과 ======

    public enum Method {
        DIRECT_USGAAP, CUSTOM, APPROX_PRETAX_INTEREST, APPROX_COMPONENTS
    }

    public static class Result {
        public final String value;
        public final String conceptUsed;
        public final Method method;
        public final USD meta; // 선택된 기간의 메타(끝일, fp/fy 등)
        public final List<String> steps = new ArrayList<>();

        private Result(String value, String conceptUsed, Method method, USD meta) {
            this.value = value;
            this.conceptUsed = conceptUsed;
            this.method = method;
            this.meta = meta;
        }
        public static Result of(double v, String c, Method m, USD u) {
            return new Result(toPlainNumberString(v), c, m, u);
        }
        public static Result of(Object v, String c, Method m, USD u) {
            return new Result(toPlainNumberString(v), c, m, u);
        }
    }

    // ====== 기간 판별 (당신 프로젝트의 SecUtil/DateUtil 스타일을 로컬 내장) ======
    static class Periods {
        static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        static boolean isQuarter(USD u) {
            if (u == null || u.getStart() == null || u.getEnd() == null) return false;

            String frame = nn(u.getFrame()).toUpperCase();
            boolean byFrame = frame.matches(".*CY\\d{4}Q[1-4]$"); // 명시적 분기 프레임만 허용
            if (frame.endsWith("YTD")) byFrame = false;          // YTD 누계는 제외

            String fp = nn(u.getFp()).toUpperCase();
            boolean byFp = fp.matches("Q[1-4]");

            long days = java.time.temporal.ChronoUnit.DAYS.between(
                    LocalDate.parse(u.getStart(), FMT),
                    LocalDate.parse(u.getEnd(), FMT)
            ) + 1;
            boolean byLen = (days >= 80 && days <= 100); // 단일 분기 기간

            // 프레임이 명시적 분기이거나, (분기표식 + 분기 기간)을 동시에 만족해야 분기로 인정
            return byFrame || (byFp && byLen);
        }

        static boolean isAnnual(USD u) {
            if (u == null) return false;
            String frame = nn(u.getFrame()).toUpperCase();
            if (frame.matches("(CY|FY)\\d{4}$")) return true;
            String fp = nn(u.getFp()).toUpperCase();
            if ("FY".equals(fp)) return true;

            String s = u.getStart(), e = u.getEnd();
            if (s != null && e != null) {
                long days = java.time.temporal.ChronoUnit.DAYS.between(
                        LocalDate.parse(s, FMT), LocalDate.parse(e, FMT)) + 1;
                return (days >= 350 && days <= 380);
            }
            return false;
        }

        static String nn(String s) { return s == null ? "" : s; }
    }

    // ====== FactAdapter ======
    /**
     * 프로젝트마다 companyfacts 파싱 구조가 다르니,
     * 아래 어댑터만 당신 환경에 맞게 구현/연결하면 나머지는 그대로 동작.
     */
    static class FactAdapter {
        /**
         * @return 해당 ns:concept의 USD 리스트(분기/연간 혼합)
         */
        @SuppressWarnings("unchecked")
        static List<USD> getUsdList(Map<String, Object> facts, String namespace, String concept) {
            if (facts == null || namespace == null || concept == null) return Collections.emptyList();
            Object factsObj = facts.get("facts");
            if (!(factsObj instanceof Map)) return Collections.emptyList();
            Map<String,Object> factsMap = (Map<String,Object>) factsObj;
            Object nsObj = factsMap.get(namespace);
            if (!(nsObj instanceof Map)) return Collections.emptyList();
            Map<String,Object> nsMap = (Map<String,Object>) nsObj;
            Object conceptObj = nsMap.get(concept);
            if (!(conceptObj instanceof Map)) return Collections.emptyList();
            Map<String,Object> conceptMap = (Map<String,Object>) conceptObj;
            Object unitsObj = conceptMap.get("units");
            if (!(unitsObj instanceof Map)) return Collections.emptyList();
            Map<String,Object> unitsMap = (Map<String,Object>) unitsObj;
            Object usdObj = unitsMap.get("USD");
            if (!(usdObj instanceof List)) return Collections.emptyList();
            List<Object> arr = (List<Object>) usdObj;
            List<USD> out = new ArrayList<>();
            for (Object o : arr) {
                if (!(o instanceof Map)) continue;
                Map<String,Object> m = (Map<String,Object>) o;
                USD u = new USD();
                try {
                    Object valObj = m.get("val");
                    if (valObj instanceof Number) {
                        u.setVal(((Number)valObj).longValue());
                    } else if (valObj != null) {
                        try { u.setVal(Long.parseLong(String.valueOf(valObj))); } catch(Exception ignore) {}
                    }
                    if (m.get("start") != null) u.setStart(String.valueOf(m.get("start")));
                    if (m.get("end") != null) u.setEnd(String.valueOf(m.get("end")));
                    if (m.get("fp") != null) u.setFp(String.valueOf(m.get("fp")));
                    if (m.get("fy") != null) u.setFy((Long) m.get("fy"));
                    if (m.get("form") != null) u.setForm(String.valueOf(m.get("form")));
                    if (m.get("filed") != null) u.setFiled(String.valueOf(m.get("filed")));
                    if (m.get("frame") != null) u.setFrame(String.valueOf(m.get("frame")));
                    if (m.get("accn") != null) u.setAccn(String.valueOf(m.get("accn")));
                } catch(Exception e) {
                    // ignore parse errors for individual fields
                }
                out.add(u);
            }
            return out;
        }

        /**
         * 전체 네임스페이스별 컨셉 목록
         */
        @SuppressWarnings("unchecked")
        static Map<String, Set<String>> listAllConceptsByNamespace(Map<String, Object> facts) {
            Map<String, Set<String>> result = new HashMap<>();
            if (facts == null) return result;
            Object factsObj = facts.get("facts");
            if (!(factsObj instanceof Map)) return result;
            Map<String,Object> factsMap = (Map<String,Object>) factsObj;
            for (Map.Entry<String,Object> nsEntry : factsMap.entrySet()) {
                String ns = nsEntry.getKey();
                Object nsObj = nsEntry.getValue();
                if (!(nsObj instanceof Map)) continue;
                Map<String,Object> nsMap = (Map<String,Object>) nsObj;
                result.put(ns, new HashSet<>(nsMap.keySet()));
            }
            return result;
        }
    }

    // ====== Helpers for multiple results extraction ======

    private static final Comparator<USD> BY_FILED_THEN_END_DESC = (a, b) -> {
        String db = coalesce(b.getFiled(), b.getEnd());
        String da = coalesce(a.getFiled(), a.getEnd());
        return compareDate(db, da);
    };

    private static <T> java.util.function.Predicate<T> distinctByKey(java.util.function.Function<? super T, ?> keyExtractor) {
        java.util.Set<Object> seen = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
        return t -> seen.add(keyExtractor.apply(t));
    }

    private static List<Result> seriesByUsGaap(Map<String, Object> facts, boolean preferQuarter, int limit) {
        List<USD> list = FactAdapter.getUsdList(facts, "us-gaap", "OperatingIncomeLoss");
        if (list == null || list.isEmpty()) return Collections.emptyList();

        Predicate<USD> filterPeriod = preferQuarter ? Periods::isQuarter : Periods::isAnnual;

        return list.stream()
                .filter(filterPeriod)
                .sorted(BY_FILED_THEN_END_DESC)
                .limit(limit)
                .map(u -> Result.of(u.getVal(), "us-gaap:OperatingIncomeLoss", Method.DIRECT_USGAAP, u))
                .collect(Collectors.toList());
    }

    private static List<Result> seriesByCustomOperating(Map<String, Object> facts, boolean preferQuarter, int limit) {
        Map<String, Set<String>> conceptsByNs = FactAdapter.listAllConceptsByNamespace(facts);
        if (conceptsByNs == null || conceptsByNs.isEmpty()) return Collections.emptyList();

        Predicate<USD> filterPeriod = preferQuarter ? Periods::isQuarter : Periods::isAnnual;

        List<USD> allCandidates = new ArrayList<>();
        Map<USD, String> usdToConcept = new HashMap<>();

        for (var e : conceptsByNs.entrySet()) {
            String ns = e.getKey();
            if (ns == null || ns.equalsIgnoreCase("us-gaap")) continue;

            for (String concept : e.getValue()) {
                String lc = concept.toLowerCase(Locale.ROOT);
                if ((lc.contains("operatingincome") || lc.contains("operatingprofit")) && !lc.contains("nonoperating")) {
                    List<USD> list = FactAdapter.getUsdList(facts, ns, concept);
                    if (list != null) {
                        for (USD u : list) {
                            allCandidates.add(u);
                            usdToConcept.put(u, ns + ":" + concept);
                        }
                    }
                }
            }
        }

        return allCandidates.stream()
                .filter(filterPeriod)
                .sorted(BY_FILED_THEN_END_DESC)
                .filter(distinctByKey(OperatingIncomeExtractor::periodKey))
                .limit(limit)
                .map(u -> Result.of(u.getVal(), usdToConcept.get(u), Method.CUSTOM, u))
                .collect(Collectors.toList());
    }

    private static List<Result> seriesByPretaxPlusInterest(Map<String, Object> facts, boolean preferQuarter, int limit) {
        List<String> pretaxCandidates = List.of(
                "IncomeLossFromContinuingOperationsBeforeIncomeTaxesExtraordinaryItemsNoncontrollingInterest",
                "IncomeLossFromContinuingOperationsBeforeIncomeTaxes"
        );
        List<String> interestExpenseCandidates = List.of(
                "InterestExpense",
                "InterestAndDebtExpense",
                "OperatingIncomeLossAndInterestExpense"
        );

        Predicate<USD> filterPeriod = preferQuarter ? Periods::isQuarter : Periods::isAnnual;

        // Build map of preTax by periodKey
        Map<String, USD> preTaxMap = new HashMap<>();
        String usedPreTax = null;
        outerPreTax:
        for (String c : pretaxCandidates) {
            List<USD> list = FactAdapter.getUsdList(facts, "us-gaap", c);
            if (list == null) continue;
            List<USD> filtered = list.stream().filter(filterPeriod).collect(Collectors.toList());
            if (!filtered.isEmpty()) {
                for (USD u : filtered) {
                    preTaxMap.put(periodKey(u), u);
                }
                usedPreTax = c;
                break outerPreTax;
            }
        }
        if (preTaxMap.isEmpty()) return Collections.emptyList();

        // Build map of interest by periodKey
        Map<String, USD> interestMap = new HashMap<>();
        String usedInterest = null;
        outerInterest:
        for (String c : interestExpenseCandidates) {
            List<USD> list = FactAdapter.getUsdList(facts, "us-gaap", c);
            if (list == null) continue;
            List<USD> filtered = list.stream().filter(filterPeriod).collect(Collectors.toList());
            if (!filtered.isEmpty()) {
                for (USD u : filtered) {
                    interestMap.put(periodKey(u), u);
                }
                usedInterest = c;
                break outerInterest;
            }
        }

        List<Result> results = new ArrayList<>();
        for (Map.Entry<String, USD> e : preTaxMap.entrySet()) {
            String key = e.getKey();
            USD preTax = e.getValue();
            USD interest = interestMap.get(key);
            double v = toDouble(preTax.getVal());
            String conceptUsed = "us-gaap:" + usedPreTax;
            if (interest != null) {
                v += toDouble(interest.getVal());
                conceptUsed += " + us-gaap:" + usedInterest;
            }
            Result r = Result.of(v, conceptUsed, Method.APPROX_PRETAX_INTEREST, preTax);
            r.steps.add("Approx: Operating ≈ PreTax + InterestExpense (동일 기간 정렬)");
            results.add(r);
        }

        return results.stream()
                .sorted((a, b) -> compareDate(coalesce(b.meta.getFiled(), b.meta.getEnd()), coalesce(a.meta.getFiled(), a.meta.getEnd())))
                .limit(limit)
                .collect(Collectors.toList());
    }

    private static List<Result> seriesByComponents(Map<String, Object> facts, boolean preferQuarter, int limit) {
        Predicate<USD> filterPeriod = preferQuarter ? Periods::isQuarter : Periods::isAnnual;

        List<USD> grossList = FactAdapter.getUsdList(facts, "us-gaap", "GrossProfit");
        if (grossList == null || grossList.isEmpty()) return Collections.emptyList();

        List<USD> grossFiltered = grossList.stream().filter(filterPeriod).collect(Collectors.toList());
        if (grossFiltered.isEmpty()) return Collections.emptyList();

        // Build maps keyed by periodKey for each component
        Map<String, USD> sgaMap = new HashMap<>();
        Map<String, USD> rAndDMap = new HashMap<>();
        Map<String, List<USD>> otherOpMap = new HashMap<>();

        List<String> otherOpCandidates = List.of(
                "OtherOperatingIncomeExpenseNet",
                "RestructuringCharges",
                "ImpairmentOfLongLivedAssets",
                "ImpairmentOfIntangibleAssets"
        );

        List<USD> sgaList = FactAdapter.getUsdList(facts, "us-gaap", "SellingGeneralAndAdministrativeExpense");
        if (sgaList != null) sgaList.stream().filter(filterPeriod).forEach(u -> sgaMap.put(periodKey(u), u));

        List<USD> rAndDList = FactAdapter.getUsdList(facts, "us-gaap", "ResearchAndDevelopmentExpense");
        if (rAndDList != null) rAndDList.stream().filter(filterPeriod).forEach(u -> rAndDMap.put(periodKey(u), u));

        for (String c : otherOpCandidates) {
            List<USD> list = FactAdapter.getUsdList(facts, "us-gaap", c);
            if (list != null) {
                list.stream().filter(filterPeriod).forEach(u -> {
                    String key = periodKey(u);
                    otherOpMap.computeIfAbsent(key, k -> new ArrayList<>()).add(u);
                });
            }
        }

        List<Result> results = new ArrayList<>();
        for (USD gross : grossFiltered) {
            String key = periodKey(gross);
            USD sga = sgaMap.get(key);
            USD rAndD = rAndDMap.get(key);
            List<USD> otherOps = otherOpMap.getOrDefault(key, Collections.emptyList());

            double otherOpSum = 0.0;
            for (USD u : otherOps) {
                otherOpSum += toDouble(u.getVal());
            }

            double v = toDouble(gross.getVal())
                    - (sga != null ? toDouble(sga.getVal()) : 0.0)
                    - (rAndD != null ? toDouble(rAndD.getVal()) : 0.0)
                    - otherOpSum;

            Result r = Result.of(v, "Constructed(Gross - SG&A - R&D - OtherOp)", Method.APPROX_COMPONENTS, gross);
            r.steps.add("Align by period with GrossProfit");
            results.add(r);
        }

        return results.stream()
                .sorted((a, b) -> compareDate(coalesce(b.meta.getFiled(), b.meta.getEnd()), coalesce(a.meta.getFiled(), a.meta.getEnd())))
                .limit(limit)
                .collect(Collectors.toList());
    }
    /** Convert a double to a non-scientific, trimmed numeric string. */
    private static String toPlainNumberString(double v) {
        return BigDecimal.valueOf(v).stripTrailingZeros().toPlainString();
    }
    /** Convert Number/String to a non-scientific numeric string; fallback to raw string or "0". */
    private static String toPlainNumberString(Object v) {
        if (v == null) return "0";
        if (v instanceof String s) {
            try {
                return new BigDecimal(s.trim()).stripTrailingZeros().toPlainString();
            } catch (Exception e) {
                // not a pure number; return as-is trimmed (still a string)
                return s.trim();
            }
        }
        if (v instanceof Number n) {
            return new BigDecimal(n.toString()).stripTrailingZeros().toPlainString();
        }
        return v.toString();
    }
}