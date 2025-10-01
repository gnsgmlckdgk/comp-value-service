package com.finance.dart.api.abroad.dto.sec.statement;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 재무제표 조회 결과 DTO
 * SEC XBRL companyconcept API (/api/xbrl/companyconcept/...) 응답 기반
 */
@Getter
@Setter
@ToString
public class CommonFinancialStatementDto {

    /** SEC 고유 기업 식별자 (CIK - Central Index Key) */
    private Long cik;

    /** XBRL taxonomy 체계 이름 (예: "us-gaap") */
    private String taxonomy;

    /** 재무제표 항목 태그 (예: "OperatingIncomeLoss") */
    private String tag;

    /** 항목 라벨 (사람이 읽기 위한 이름, 예: "Operating Income or Loss") */
    private String label;

    /** 항목 설명 (해당 태그가 의미하는 상세 설명) */
    private String description;

    /** 기업명 (예: "Apple Inc.") */
    private String entityName;

    /** 단위별 데이터 값들 (예: USD 기준 연도별 영업이익 등) */
    private Units units;
}