package com.finance.dart.api.abroad.util;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 유동자산 합계 추출 유틸리티
 */
public class AssetsCurrentExtractor {

    // US-GAAP 표준 태그
    private static final String USGAAP_ASSETS_CURRENT = "us-gaap:AssetsCurrent";
    // IFRS 표준 태그
    private static final String IFRS_ASSETS_CURRENT = "ifrs-full:CurrentAssets";

    /**
     * companyfacts Map에서 유동자산 합계 추출
     *
     * @param facts SEC companyfacts JSON을 변환한 Map
     * @return 가장 최신 유동자산 합계 값(String), 없으면 null
     */
    @SuppressWarnings("unchecked")
    public static String extract(Map<String, Object> facts) {
        if (facts == null || facts.isEmpty()) return null;

        // 1) US-GAAP 표준 태그 확인
        String result = extractByConcept(facts, USGAAP_ASSETS_CURRENT);
        if (result != null) return result;

        // 2) IFRS 표준 태그 확인
        result = extractByConcept(facts, IFRS_ASSETS_CURRENT);
        if (result != null) return result;

        // 3) 커스텀 네임스페이스 탐색 (concept 이름에 AssetsCurrent / CurrentAssets 포함)
        result = extractByCustomConcept(facts);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static String extractByConcept(Map<String, Object> facts, String concept) {
        try {
            Map<String, Object> ns = (Map<String, Object>) facts.get("facts");
            if (ns == null) return null;

            // 네임스페이스: us-gaap, ifrs-full, custom 등
            for (Map.Entry<String, Object> entry : ns.entrySet()) {
                Map<String, Object> concepts = (Map<String, Object>) entry.getValue();
                if (concepts == null) continue;

                if (concepts.containsKey(concept.replace(entry.getKey() + ":", ""))) {
                    Map<String, Object> conceptData = (Map<String, Object>) concepts.get(concept.replace(entry.getKey() + ":", ""));
                    if (conceptData == null) continue;

                    List<Map<String, Object>> units = (List<Map<String, Object>>) conceptData.get("units");
                    if (units == null) continue;

                    // "USD" 단위 데이터만 취급
                    List<Map<String, Object>> usdData = units.stream()
                            .filter(u -> "USD".equalsIgnoreCase((String) u.get("uom")))
                            .collect(Collectors.toList());
                    if (usdData.isEmpty()) continue;

                    // 최신 endDate 기준 정렬 후 첫 값
                    usdData.sort((a, b) -> {
                        String endA = (String) a.get("end");
                        String endB = (String) b.get("end");
                        return endB.compareTo(endA); // 최신순
                    });

                    Object val = usdData.get(0).get("val");
                    return val != null ? String.valueOf(val) : null;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static String extractByCustomConcept(Map<String, Object> facts) {
        try {
            Map<String, Object> ns = (Map<String, Object>) facts.get("facts");
            if (ns == null) return null;

            for (Map.Entry<String, Object> entry : ns.entrySet()) {
                Map<String, Object> concepts = (Map<String, Object>) entry.getValue();
                if (concepts == null) continue;

                for (String conceptKey : concepts.keySet()) {
                    String keyLower = conceptKey.toLowerCase();
                    if (keyLower.contains("assetscurrent") || keyLower.contains("currentassets")) {
                        Map<String, Object> conceptData = (Map<String, Object>) concepts.get(conceptKey);
                        if (conceptData == null) continue;

                        List<Map<String, Object>> units = (List<Map<String, Object>>) conceptData.get("units");
                        if (units == null) continue;

                        List<Map<String, Object>> usdData = units.stream()
                                .filter(u -> "USD".equalsIgnoreCase((String) u.get("uom")))
                                .collect(Collectors.toList());
                        if (usdData.isEmpty()) continue;

                        usdData.sort((a, b) -> {
                            String endA = (String) a.get("end");
                            String endB = (String) b.get("end");
                            return endB.compareTo(endA); // 최신순
                        });

                        Object val = usdData.get(0).get("val");
                        return val != null ? String.valueOf(val) : null;
                    }
                }
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}