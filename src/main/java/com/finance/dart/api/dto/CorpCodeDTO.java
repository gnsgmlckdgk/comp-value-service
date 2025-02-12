package com.finance.dart.api.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class CorpCodeDTO {

    @SerializedName("corp_code")
    private String corpCode;

    @SerializedName("corp_name")
    private String corpName;

    @SerializedName("stock_code")
    private String stockCode;

    @SerializedName("modify_date")
    private String modifyDate;

    /**
     * 1주당 가치
     */
    private String oneStockValue;

}
