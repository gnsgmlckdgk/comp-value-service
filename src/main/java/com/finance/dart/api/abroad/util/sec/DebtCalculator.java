package com.finance.dart.api.abroad.util.sec;

import com.finance.dart.api.abroad.dto.sec.statement.USD;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 * 부채 계산기
 * 총부채와 유동부채에서 같은 회계 재무정보끼리 계산한 고정부채 값을 반환
 * </pre>
 */
public class DebtCalculator {

    /**
     * 10-Q : 분기보고서
     * 10-K : 연차보고서
     */
    private static final Set<String> ALLOWED_FORMS = Set.of("10-Q", "10-K");

    /**
     * 총부채(파생) = 자산 − 전체지분
     * @param assets
     * @param totalEquity
     * @return
     */
    public static List<USD> calculateLiabilitiesFromAssetsAndEquityRobust(
            List<USD> assets, List<USD> totalEquity
    ) {
        List<USD> aSan = sanitizeForCalc(assets);
        List<USD> eSan = sanitizeForCalc(totalEquity);

        Map<String, USD> aLatest = pickLatestByKey(aSan); // accn|end|frame 기준, filed 최신본
        Map<String, USD> eLatest = pickLatestByKey(eSan);

        Set<String> processed = new HashSet<>();
        List<USD> out = new ArrayList<>();

        // 자산 기준 차감
        for (USD a : aLatest.values()) {
            String k = key(a);
            USD e = eLatest.get(k);
            if (e == null) {
                e = findByAccnEndFallback(eSan, a.getAccn(), a.getEnd());
            }
            out.add(subtractOrSingle(a, e));
            processed.add(k);
        }

        // 지분만 있는 키 처리
        for (USD e : eLatest.values()) {
            String k = key(e);
            if (processed.contains(k)) continue;

            USD a = aLatest.get(k);
            if (a == null) {
                a = findByAccnEndFallback(aSan, e.getAccn(), e.getEnd());
            }
            out.add(subtractOrSingle(a, e));
        }

        // 정렬: end 내림차순 → filed 내림차순
        out.sort(Comparator
                .comparing(DebtCalculator::safeEnd).reversed()
                .thenComparing(DebtCalculator::filedAsDate).reversed());

        // null 요소 방지 (방어적으로)
        return out.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }


    /**
     * <pre>
     * 고정부채 계산
     * 총부채 - 유동부채 (같은 accn + end + frame 기준, 중복 시 filed 최신건 사용)
     * </pre>
     * @param totalLiabilities 총부채 데이터
     * @param currentLiabilities 고정부채 데이터
     * @return
     */
    public static List<USD> calculateNonCurrentLiabilitiesRobust(
            List<USD> totalLiabilities, List<USD> currentLiabilities) {

        // 1) 정제: 10-Q/10-K만, 필수키 존재
        List<USD> totals = sanitize(totalLiabilities);
        List<USD> currents = sanitize(currentLiabilities);

        // 2) (accn|end|frame) 키로 묶고, filed 최신건 선택
        Map<String, USD> totalLatest = pickLatestByKey(totals);
        Map<String, USD> currentLatest = pickLatestByKey(currents);

        List<USD> out = new ArrayList<>();

        for (USD t : totalLatest.values()) {
            String k = key(t);
            USD c = currentLatest.get(k);

            // frame 불일치로 못 찾는 경우, frame 무시 fallback (accn|end)로 2차 탐색
            if (c == null) {
                c = findByAccnEndFallback(currents, t.getAccn(), t.getEnd());
            }

            if (c != null && t.getVal() != null && c.getVal() != null) {
                long diff = t.getVal() - c.getVal();

                USD nonCurrent = new USD();
                nonCurrent.setAccn(t.getAccn());
                nonCurrent.setEnd(t.getEnd());
                nonCurrent.setFiled(maxFiled(t.getFiled(), c.getFiled())); // 참고용
                nonCurrent.setForm(t.getForm());
                nonCurrent.setFp(t.getFp());
                nonCurrent.setFy(t.getFy());
                nonCurrent.setStart(t.getStart());
                nonCurrent.setFrame(t.getFrame());
                nonCurrent.setVal(diff);

                out.add(nonCurrent);
            }
        }
        return out;
    }

    private static List<USD> sanitize(List<USD> list) {
        if (list == null) return List.of();
        return list.stream()
                .filter(u -> u != null
                        && u.getAccn() != null
                        && u.getEnd() != null
                        && u.getForm() != null
                        && ALLOWED_FORMS.contains(u.getForm()))
                .collect(Collectors.toList());
    }

    private static Map<String, USD> pickLatestByKey(List<USD> list) {
        return list.stream()
                .collect(Collectors.groupingBy(
                        DebtCalculator::key,
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparing(DebtCalculator::filedAsDate)
                                        .thenComparing(USD::getEnd)), // 동일 filed면 end로 tie-break
                                Optional::get
                        )));
    }

    // 키: accn|end|frame(없으면 빈문자)
    private static String key(USD u) {
        String frame = u.getFrame() == null ? "" : u.getFrame();
        return u.getAccn() + "|" + u.getEnd() + "|" + frame;
    }

    private static LocalDate filedAsDate(USD u) {
        try {
            return (u.getFiled() != null) ? LocalDate.parse(u.getFiled()) : LocalDate.MIN;
        } catch (Exception e) {
            return LocalDate.MIN;
        }
    }

    private static String maxFiled(String a, String b) {
        LocalDate da = safeParse(a), db = safeParse(b);
        return (da.isAfter(db)) ? a : b;
    }

    private static LocalDate safeParse(String s) {
        try { return (s != null) ? LocalDate.parse(s) : LocalDate.MIN; }
        catch (Exception e) { return LocalDate.MIN; }
    }

    private static USD findByAccnEndFallback(List<USD> currents, String accn, String end) {
        // 같은 accn+end 중 최신 filed
        return currents.stream()
                .filter(u -> Objects.equals(accn, u.getAccn()) && Objects.equals(end, u.getEnd()))
                .max(Comparator.comparing(DebtCalculator::filedAsDate))
                .orElse(null);
    }


    /** 계산용 경량 정제: accn/end/form 존재만 확인 (form 필터링 없음) */
    private static List<USD> sanitizeForCalc(List<USD> list) {
        if (list == null) return List.of();
        return list.stream()
                .filter(u -> u != null
                        && u.getAccn() != null
                        && u.getEnd() != null
                        && u.getForm() != null)
                .collect(Collectors.toList());
    }

    /** (자산 - 지분) 계산. 한쪽만 있으면 단독값 복제 반환 */
    private static USD subtractOrSingle(USD a, USD e) {
        if (a == null && e == null) return null;
        if (a == null) return cloneUsd(e); // 지분만 있으면 지분 단독 반환
        if (e == null) return cloneUsd(a); // 자산만 있으면 자산 단독 반환

        long aVal = safeVal(a.getVal());
        long eVal = safeVal(e.getVal());
        long diff = aVal - eVal;

        USD newer = filedIsAfter(a.getFiled(), e.getFiled()) ? a : e;

        USD out = cloneUsd(newer);
        out.setVal(diff);
        // 키 일관성: 자산(a) 기준
        out.setAccn(a.getAccn());
        out.setEnd(a.getEnd());
        out.setFrame(a.getFrame());
        out.setFy(a.getFy());
        out.setFp(a.getFp());
        out.setForm(a.getForm());
        out.setStart(a.getStart());
        out.setFiled(maxFiled(a.getFiled(), e.getFiled()));
        return out;
    }

    /** end 날짜 null-safe 파싱 (정렬용) */
    private static LocalDate safeEnd(USD u) {
        try {
            return (u.getEnd() != null) ? LocalDate.parse(u.getEnd()) : LocalDate.MIN;
        } catch (Exception e) {
            return LocalDate.MIN;
        }
    }

    /** filed 문자열 비교: a 가 b 보다 최신이면 true */
    private static boolean filedIsAfter(String a, String b) {
        LocalDate da = safeParse(a), db = safeParse(b);
        return da.isAfter(db);
    }

    /** null-safe long 값 */
    private static long safeVal(Long v) {
        return v == null ? 0L : v;
    }

    /** USD 얕은 복사 */
    private static USD cloneUsd(USD src) {
        USD d = new USD();
        d.setAccn(src.getAccn());
        d.setEnd(src.getEnd());
        d.setStart(src.getStart());
        d.setFy(src.getFy());
        d.setFp(src.getFp());
        d.setForm(src.getForm());
        d.setFrame(src.getFrame());
        d.setFiled(src.getFiled());
        d.setVal(src.getVal());
        return d;
    }
}
