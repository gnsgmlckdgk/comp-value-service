package com.finance.dart.dto;

import lombok.Data;

import java.util.List;


/**
 * 공시정보 응답 DTO
 * https://opendart.fss.or.kr/guide/detail.do?apiGrpCd=DS001&apiId=2019001
 */
@Data
public class DisclosuerInfoResDTO {

    /**
     * 에러 및 정보 코드
     */
    private String status = "";

    /**
     * 에러 및 정보 메시지
     */
    private String message = "";

    /**
     * 페이지 번호
     */
    private String page_no = "";

    /**
     * 페이지 별 건수
     */
    private String page_count = "";

    /**
     * 총 건
     */
    private String total_count = "";

    /**
     * 총 페이지 수
     */
    private String total_page = "";


    private List<DisclosureInfoListResDTO> list;

    @Data
    private static class DisclosureInfoListResDTO {

        /**
         * 법인구분
         */
        private String corp_cls;
        /**
         * 종목명(법인명)
         */
        private String corp_name;
        /**
         * 고유번호
         */
        private String corp_code;
        /**
         * 종목코드
         */
        private String stock_code;
        /**
         * 보고서명
         */
        private String report_nm;
        /**
         * 접수번호
         */
        private String rcept_no;
        /**
         * 공시 제출인명
         */
        private String flr_nm;
        /**
         * 접수일자
         */
        private String rcept_dt;
        /**
         * 비고
         */
        private String rm;

    }
}




