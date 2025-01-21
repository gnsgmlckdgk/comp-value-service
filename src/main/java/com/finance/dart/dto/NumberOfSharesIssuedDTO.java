package com.finance.dart.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class NumberOfSharesIssuedDTO {

    @SerializedName("rcept_no")
    private String rcept_no;

    @SerializedName("corp_cls")
    private String corp_cls;

    @SerializedName("corp_code")
    private String corp_code;

    @SerializedName("corp_name")
    private String corp_name;

    @SerializedName("se")
    private String se;

    @SerializedName("isu_stock_totqy")
    private String isu_stock_totqy;

    @SerializedName("now_to_isu_stock_totqy")
    private String now_to_isu_stock_totqy;

    @SerializedName("now_to_dcrs_stock_totqy")
    private String now_to_dcrs_stock_totqy;

    @SerializedName("redc")
    private String redc;

    @SerializedName("profit_incnr")
    private String profit_incnr;

    @SerializedName("rdmstk_repy")
    private String rdmstk_repy;

    @SerializedName("etc")
    private String etc;

    @SerializedName("istc_totqy")
    private String istc_totqy;

    @SerializedName("tesstk_co")
    private String tesstk_co;

    @SerializedName("distb_stock_co")
    private String distb_stock_co;

    @SerializedName("stlm_dt")
    private String stlm_dt;
}
