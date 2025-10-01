package com.finance.dart.api.abroad.util.sec;

import com.finance.dart.api.abroad.dto.sec.statement.CommonFinancialStatementDto;
import com.finance.dart.api.abroad.dto.sec.statement.Shares;
import com.finance.dart.api.abroad.dto.sec.statement.USD;
import com.finance.dart.api.abroad.dto.sec.statement.Units;
import com.finance.dart.common.util.DateUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * SEC API 유틸
 */
public class SecUtil {

    private final static String CONV_CIK_ID = "{cik}";

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // 대략 분기 기간 허용 범위(윤년/52·53주 회계 보정 폭)
    private static final int Q_MIN = 75, Q_MAX = 115;

    /**
     * URL에 CIK 세팅
     * @param url
     * @param cik
     * @return
     */
    public static String setUrlCik(String url, String cik) {
        return url.replace(CONV_CIK_ID, cik);
    }

    /**
     * CommonFinancialStatementDto → Units → USD 리스트를 null-safe 하게 추출한다.
     * 값이 없으면 빈 리스트를 반환한다.
     */
    public static List<USD> getUsdList(CommonFinancialStatementDto dto) {
        return Optional.ofNullable(dto)
                .map(CommonFinancialStatementDto::getUnits)
                .map(Units::getUsd)
                .orElse(Collections.emptyList());
    }

    /**
     * usdList에서 날짜 기준으로 가장 최근(offset=0), 바로 이전(offset=1), 그 이전(offset=2) 데이터를 반환
     * @param usdList USD 리스트
     * @param offset  0=최근, 1=바로 이전, 2=그 이전...
     * @return 해당 위치의 USD, 없으면 null
     */
    public static USD getUsdByOffset(List<USD> usdList, int offset) {
        if (usdList == null || usdList.isEmpty() || offset < 0) return null;

        return usdList.stream()
                .sorted((u1, u2) -> DateUtil.compareDate(u2.getEnd(), u1.getEnd())) // 최신순 정렬 (음수면 앞에 배치)
                .skip(offset) // offset 만큼 건너뛰기
                .findFirst()
                .orElse(null);
    }

    /**
     * <pre>
     * usdList에서 날짜 기준으로 가장 최근(offset=0), 바로 이전(offset=1), 그 이전(offset=2) 데이터를 반환
     * 분기데이터만 조회
     * </pre>
     * @param usdList
     * @param offset
     * @return
     */
    public static USD getQuarterUsdByOffset(List<USD> usdList, int offset) {
        if (usdList == null || usdList.isEmpty() || offset < 0) return null;

        return usdList.stream()
                .filter(SecUtil::isQuarter)
                .sorted((u1, u2) -> DateUtil.compareDate(u2.getEnd(), u1.getEnd())) // 최신순 정렬 (음수면 앞에 배치)
                .skip(offset) // offset 만큼 건너뛰기
                .findFirst()
                .orElse(null);
    }

    /**
     * <pre>
     * usdList에서 날짜 기준으로 가장 최근(offset=0), 바로 이전(offset=1), 그 이전(offset=2) 데이터를 반환
     * 연간데이터만 조회
     * </pre>
     * @param usdList
     * @param offset
     * @return
     */
    public static USD getAnnualUsdByOffset(List<USD> usdList, int offset) {
        if (usdList == null || usdList.isEmpty() || offset < 0) return null;

        return usdList.stream()
                .filter(SecUtil::isAnnual)
                .sorted((u1, u2) -> DateUtil.compareDate(u2.getEnd(), u1.getEnd())) // 최신순 정렬 (음수면 앞에 배치)
                .skip(offset) // offset 만큼 건너뛰기
                .findFirst()
                .orElse(null);
    }

    /**
     * USD 자료가 분기 데이터인지 판단
     * @param u
     * @return
     */
    public static boolean isQuarter(USD u) {
        if (u == null || u.getStart() == null || u.getEnd() == null) return false;

        long days = ChronoUnit.DAYS.between(LocalDate.parse(u.getStart(), FMT),
                LocalDate.parse(u.getEnd(), FMT));

        // 1) 기간이 분기 범위면 분기로 본다
        if (days >= Q_MIN && days <= Q_MAX) return true;

        // 2) 보조: frame이 ...Q1|Q2|Q3|Q4 형태(끝이 YTD가 아님)면 분기로 간주
        String frame = u.getFrame();
        if (frame != null) {
            String f = frame.trim().toUpperCase();
            if (f.matches(".*Q[1-4]$")) return true;      // CY2025Q2 등
            if (f.matches(".*Q[1-4]YTD$")) return false;  // CY2025Q2YTD 등은 누적
        }
        return false;
    }

    /**
     * USD 자료가 연간 데이터인지 판단
     * @param u
     * @return
     */
    public static boolean isAnnual(USD u) {
        if (u == null || u.getStart() == null || u.getEnd() == null) return false;

        // 0) 분기/누적(YTD) 패턴은 연간이 아님
        String frameRaw = u.getFrame();
        if (frameRaw != null) {
            String f = frameRaw.trim().toUpperCase();
            if (f.matches(".*Q[1-4](YTD)?$")) return false;  // CY2025Q2, CY2025Q2YTD 등은 제외
            if (f.endsWith("YTD")) return false;            // 기타 YTD도 제외
        }

        // 1) 기간 길이로 연간 판별 (윤년/회계달 변동 고려하여 여유 범위)
        final int ANNUAL_MIN = 350; // ≈ 12개월 - 여유
        final int ANNUAL_MAX = 380; // ≈ 12개월 + 여유
        long days = ChronoUnit.DAYS.between(
                LocalDate.parse(u.getStart(), FMT),
                LocalDate.parse(u.getEnd(),   FMT)
        ) + 1; // 양끝 포함
        if (days >= ANNUAL_MIN && days <= ANNUAL_MAX) return true;

        // 2) 메타데이터로 보조 판별
        String fp = u.getFp() == null ? "" : u.getFp().trim().toUpperCase();
        if ("FY".equals(fp)) return true;

        String f = frameRaw == null ? "" : frameRaw.trim().toUpperCase();
        if (f.matches("(CY|FY)\\d{4}$")) return true; // CY2024, FY2023 등

        return false;
    }


    /**
     * CommonFinancialStatementDto → Units → shares 리스트를 null-safe 하게 추출한다.
     * 값이 없으면 빈 리스트를 반환한다.
     */
    public static List<Shares> getSharesList(CommonFinancialStatementDto dto) {
        return Optional.ofNullable(dto)
                .map(CommonFinancialStatementDto::getUnits)
                .map(Units::getShares)
                .orElse(Collections.emptyList());
    }

    /**
     * sharesList 에서 날짜 기준으로 가장 최근(offset=0), 바로 이전(offset=1), 그 이전(offset=2) 데이터를 반환
     * @param sharesList USD 리스트
     * @param offset  0=최근, 1=바로 이전, 2=그 이전...
     * @return 해당 위치의 USD, 없으면 null
     */
    public static Shares getSharesByOffset(List<Shares> sharesList, int offset) {
        if (sharesList == null || sharesList.isEmpty() || offset < 0) return null;

        return sharesList.stream()
                .sorted((u1, u2) -> DateUtil.compareDate(u2.getEnd(), u1.getEnd())) // 최신순 정렬 (음수면 앞에 배치)
                .skip(offset) // offset 만큼 건너뛰기
                .findFirst()
                .orElse(null);
    }

}
