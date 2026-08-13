package com.finance.dart.api.common.util;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 증권 유형 판별 유틸
 * - 비일반주식(워런트/우선주/CVR/채권/ETD 등) 감지 — 추천 필터 + 평가 품질 게이트 공용
 */
public final class SecurityTypeUtil {

    private SecurityTypeUtil() {}

    /** 비일반주식 심볼 패턴 (워런트/우선주/CVR/유닛 등) */
    private static final Pattern NON_COMMON_STOCK_SYMBOL_PATTERN = Pattern.compile(
            ".*[-+](W[SB]?|WT|RT|RI|UN)$"              // 하이픈 뒤 접미사: -W(워런트), -WT, -RI(CVR), -UN(유닛)
            + "|.*[-+]P[A-Z]?$"                          // 하이픈 뒤 우선주: -P, -PA 등 (BRK-A/B 클래스주식 제외)
            + "|.{3,}W[SW]$"                             // 워런트: SBCWW 등 (5글자 이상)
            + "|.{3,}PR[A-Z]$"                           // 우선주: BACPRL 등 (하이픈 없는 6글자 이상)
    );

    /** 비일반주식 기업명 키워드 (소문자 비교) */
    private static final List<String> NON_COMMON_STOCK_NAME_KEYWORDS = List.of(
            "warrant", "warrants",
            "preferred", "preference",
            "contingent value right",
            "first mortgage", "mortgage bond",
            "series due", "% series",
            "debenture", "subordinated note", "junior subordinated",
            "depositary share",
            "capital trust",
            " bond", " bonds",
            " notes", " note "
    );

    /** 비일반주식 기업명 정규식 - 이자율, 만기일, ETD 채권 표기(1M BD 등) */
    private static final Pattern NON_COMMON_STOCK_NAME_PATTERN = Pattern.compile(
            "\\d+\\.?\\d*\\s*%"               // 이자율: "5.35%" 등 (채권/노트)
            + "|\\bdue\\s+20\\d{2}\\b"         // 만기일: "due 2027" 등
            + "|\\b\\d+\\s*m\\s+bd\\b"          // ETD 채권 표기: "1M BD 66"(1st mortgage bond) 등
            + "|\\bsr\\s+nts?\\b"              // Senior Notes 약어
    );

    /**
     * 이름/심볼 패턴으로는 잡히지 않는 알려진 ETD(거래소 상장 채권/베이비본드) 심볼 블록리스트.
     * - Entergy 계열 1st mortgage bond ETD (기업명이 "Entergy ..., LLC"라 키워드 미검출).
     * - 새 ETD 발견 시 여기에 추가.
     */
    private static final Set<String> KNOWN_ETD_SYMBOLS = Set.of(
            "ENJ", "ENO", "EMP"   // Entergy New Orleans/Mississippi 1st mortgage bond ETD
    );

    /**
     * 비일반주식(워런트/우선주/CVR/채권/ETD) 여부 판단
     */
    public static boolean isNonCommonStock(String symbol, String companyName) {
        if (symbol != null) {
            if (KNOWN_ETD_SYMBOLS.contains(symbol.toUpperCase())) {
                return true;
            }
            if (NON_COMMON_STOCK_SYMBOL_PATTERN.matcher(symbol).matches()) {
                return true;
            }
        }
        if (companyName != null) {
            String nameLower = companyName.toLowerCase();
            for (String keyword : NON_COMMON_STOCK_NAME_KEYWORDS) {
                if (nameLower.contains(keyword)) {
                    return true;
                }
            }
            if (NON_COMMON_STOCK_NAME_PATTERN.matcher(nameLower).find()) {
                return true;
            }
        }
        return false;
    }
}
