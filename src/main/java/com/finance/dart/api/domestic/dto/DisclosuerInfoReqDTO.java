package com.finance.dart.api.domestic.dto;

import lombok.Data;


/**
 * 공시정보 요청 DTO
 * https://opendart.fss.or.kr/guide/detail.do?apiGrpCd=DS001&apiId=2019001
 */
@Data
public class DisclosuerInfoReqDTO {

    /**
     * 고유번호
     */
    private String corp_code = "";

    /**
     * 시작일
     */
    private String bgn_de = "";

    /**
     * 종료일
     */
    private String end_de = "";

    /**
     * 최종보고서 검색여부
     */
    private String last_reprt_at = "";

    /**
     * 공시유형
     */
    private String pblntf_ty = "";

    /**
     * 공시상세유형
     */
    private String pblntf_detail_ty = "";

    /**
     * 법인구분
     */
    private String corp_cls = "";

    /**
     * 정렬
     */
    private String sort = "";

    /**
     * 정렬방법
     */
    private String sort_mth = "";

    /**
     * 페이지 번호
     */
    private String page_no = "";

    /**
     * 페이지 별 건수
     */
    private String page_count = "";

}
