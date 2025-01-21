package com.finance.dart.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class FinancialStatementDTO {

    @SerializedName("rcept_no")
    private String rceptNo;

    @SerializedName("reprt_code")
    private String reprtCode;

    @SerializedName("bsns_year")
    private String bsnsYear;

    @SerializedName("corp_code")
    private String corpCode;

    @SerializedName("sj_div")
    private String sjDiv;

    @SerializedName("sj_nm")
    private String sjNm;

    @SerializedName("account_id")
    private String accountId;

    @SerializedName("account_nm")
    private String accountNm;

    @SerializedName("account_detail")
    private String accountDetail;

    @SerializedName("thstrm_nm")
    private String thstrmNm;

    @SerializedName("thstrm_amount")
    private String thstrmAmount;

    @SerializedName("thstrm_add_amount")
    private String thstrmAddAmount;

    @SerializedName("frmtrm_nm")
    private String frmtrmNm;

    @SerializedName("frmtrm_amount")
    private String frmtrmAmount;

    @SerializedName("frmtrm_q_nm")
    private String frmtrm_q_nm;

    @SerializedName("frmtrm_q_amount")
    private String frmtrm_q_amount;

    @SerializedName("frmtrm_add_amount")
    private String frmtrmAddAmount;

    @SerializedName("bfefrmtrm_nm")
    private String bfefrmtrmNm;

    @SerializedName("bfefrmtrm_amount")
    private String bfefrmtrmAmount;

    @SerializedName("ord")
    private String ord;

    @SerializedName("currency")
    private String currency;
}
