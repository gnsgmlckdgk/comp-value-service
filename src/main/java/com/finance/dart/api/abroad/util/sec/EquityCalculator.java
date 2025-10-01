package com.finance.dart.api.abroad.util.sec;

import com.finance.dart.api.abroad.dto.sec.statement.USD;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 * 지분 계산기
 * 지배주주지분(StockholdersEquity) 과 비지배주주지분(NoncontrollingInterest)을
 * 같은 회계 재무정보(accn|end|frame) 기준으로 합산하여
 * 전체지분(= StockholdersEquityIncludingPortionAttributableToNoncontrollingInterest 상당)을 산출.
 *
 * 규칙:
 * - 10-Q / 10-K 만 사용
 * - 같은 accn|end|frame 에 중복값이 있으면 filed 최신건만 채택
 * - frame 불일치로 매칭 실패 시 accn|end 기반으로 2차(fallback) 매칭
 * - 한쪽에만 있는 기간은 그 값 그대로 포함(합집합)
 * </pre>
 */
public class EquityCalculator {

    /**
     * 10-Q : 분기보고서
     * 10-K : 연차보고서
     */
    private static final Set<String> ALLOWED_FORMS = Set.of("10-Q", "10-K");

    /**
     * <pre>
     * 전체지분 계산
     * 지배주주지분 + 비지배주주지분 (같은 accn|end|frame 기준, 중복 시 filed 최신건 사용)
     * frame 불일치 시 accn|end fallback로 매칭 시도
     * 한쪽만 존재하는 기간은 해당 값만 결과에 포함
     * </pre>
     * @param stockholdersEquity 지배주주 지분(StockholdersEquity)
     * @param noncontrollingInterest 비지배주주 지분(NoncontrollingInterest)
     * @return 합산 결과 리스트(USD)
     */
    public static List<USD> calculateTotalEquityRobust(
            List<USD> stockholdersEquity,
            List<USD> noncontrollingInterest
    ) {
        // 1) 정제: 10-Q/10-K만, 필수키 존재
        List<USD> holders = sanitize(stockholdersEquity);
        List<USD> nonctrl = sanitize(noncontrollingInterest);

        // 2) (accn|end|frame) 키로 묶고, filed 최신건 선택
        Map<String, USD> holdersLatest = pickLatestByKey(holders);
        Map<String, USD> nonctrlLatest = pickLatestByKey(nonctrl);

        // 3) 합집합 키
        Set<String> processedKeys = new HashSet<>();
        List<USD> out = new ArrayList<>();

        // 3-1) holders 기준으로 합산
        for (USD h : holdersLatest.values()) {
            String k = key(h);
            USD n = nonctrlLatest.get(k);

            // frame 불일치로 못 찾으면 accn|end 로 fallback
            if (n == null) {
                n = findByAccnEndFallback(nonctrl, h.getAccn(), h.getEnd());
            }

            out.add(sumOrSingle(h, n));
            processedKeys.add(k);
        }

        // 3-2) nonctrl 쪽에만 있는 키 추가 (holders에 없던 것들)
        for (USD n : nonctrlLatest.values()) {
            String k = key(n);
            if (processedKeys.contains(k)) continue;

            // holders 쪽에서 frame 불일치로 못 찾았을 수 있으니 fallback도 확인
            USD h = holdersLatest.get(k);
            if (h == null) {
                h = findByAccnEndFallback(holders, n.getAccn(), n.getEnd());
            }

            out.add(sumOrSingle(h, n));
        }

        // 4) 정렬: end 내림차순 → filed 내림차순 (가독성)
        out.sort(Comparator
                .comparing(EquityCalculator::safeEnd).reversed()
                .thenComparing(EquityCalculator::filedAsDate).reversed());

        return out;
    }

    /** h, n 모두 null이면 null 반환 (호출부에서 넣지 않음). 하나만 있으면 그 값 그대로 복제 반환. 둘 다 있으면 합산하여 반환. */
    private static USD sumOrSingle(USD h, USD n) {
        if (h == null && n == null) return null;
        if (h == null) return cloneUsd(n); // 한쪽만 존재
        if (n == null) return cloneUsd(h); // 한쪽만 존재

        long hVal = safeVal(h.getVal());
        long nVal = safeVal(n.getVal());
        long sum = hVal + nVal;

        // 메타데이터는 filed가 더 최신인 쪽 기준으로
        USD newer = filedIsAfter(h.getFiled(), n.getFiled()) ? h : n;

        USD out = cloneUsd(newer);
        out.setVal(sum);
        // accn|end|frame은 가능한 한 '키가 일치했던' h 기준을 유지 (가독성/일관성)
        out.setAccn(h.getAccn());
        out.setEnd(h.getEnd());
        out.setFrame(h.getFrame());
        out.setFy(h.getFy());
        out.setFp(h.getFp());
        out.setForm(h.getForm());
        out.setStart(h.getStart());

        // filed는 두 값 중 더 최신으로
        out.setFiled(maxFiled(h.getFiled(), n.getFiled()));
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
                        EquityCalculator::key,
                        LinkedHashMap::new,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator
                                        .comparing(EquityCalculator::filedAsDate)
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

    private static LocalDate safeEnd(USD u) {
        try {
            return (u.getEnd() != null) ? LocalDate.parse(u.getEnd()) : LocalDate.MIN;
        } catch (Exception e) {
            return LocalDate.MIN;
        }
    }

    private static String maxFiled(String a, String b) {
        LocalDate da = safeParse(a), db = safeParse(b);
        return (da.isAfter(db)) ? a : b;
    }

    private static boolean filedIsAfter(String a, String b) {
        LocalDate da = safeParse(a), db = safeParse(b);
        return da.isAfter(db);
    }

    private static LocalDate safeParse(String s) {
        try { return (s != null) ? LocalDate.parse(s) : LocalDate.MIN; }
        catch (Exception e) { return LocalDate.MIN; }
    }

    /** frame 불일치 등으로 키가 달라진 경우, 같은 accn+end 중 최신 filed를 선택 */
    private static USD findByAccnEndFallback(List<USD> list, String accn, String end) {
        return list.stream()
                .filter(u -> Objects.equals(accn, u.getAccn()) && Objects.equals(end, u.getEnd()))
                .max(Comparator.comparing(EquityCalculator::filedAsDate))
                .orElse(null);
    }

    /** null-safe long 값 */
    private static long safeVal(Long v) {
        return v == null ? 0L : v;
    }

    /** USD 얕은 복사 + 필요한 필드 유지 */
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
