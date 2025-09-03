package com.finance.dart.api.abroad.util;

import com.finance.dart.api.abroad.dto.financial.statement.USD;

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
}
