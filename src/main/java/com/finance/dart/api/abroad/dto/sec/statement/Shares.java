package com.finance.dart.api.abroad.dto.sec.statement;

import lombok.Data;

@Data
public class Shares {

    /** Accession Number – 해당 보고서의 고유 식별자 (예: "0000320193-23-000066") */
    private String accn;

    /** 회계 기간 종료일 (예: "2023-09-30") */
    private String end;

    /** SEC에 보고서가 실제 제출된 날짜 (예: "2023-10-26") */
    private String filed;

    /** 보고서 종류 (예: "10-K", "10-Q") */
    private String form;

    /** 회계 기간 구분 (Fiscal Period), 예: "Q1", "Q2", "Q3", "FY" */
    private String fp;

    /** 회계 연도 (Fiscal Year), 예: 2023 */
    private Long fy;

    /** 회계 기간 시작일 (예: "2023-07-01") – 일부 항목에서만 제공될 수 있음 */
    private String start;

    /** 해당 항목의 실제 값 (예: 영업이익이 112,000,000,000 USD) */
    private Long val;

    /** 데이터가 어떤 기간인지 (예: CY2025Q2 → Calendar Year 2025, 2분기 (3개월 기간)) **/
    private String frame;
}
