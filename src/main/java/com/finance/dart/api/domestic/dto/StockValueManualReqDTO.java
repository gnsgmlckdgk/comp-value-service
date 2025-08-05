package com.finance.dart.api.domestic.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
public class StockValueManualReqDTO {

    /**
     * 단위
     */
    @SerializedName("unit")
    private String unit;

    /**
     * 전전기 영업이익
     */
    @SerializedName("operatingProfitPrePre")
    private String operatingProfitPrePre;

    /**
     * 전기 영업이익
     */
    @SerializedName("operatingProfitPre")
    private String operatingProfitPre;

    /**
     * 당기 영업이익
     */
    @SerializedName("operatingProfitCurrent")
    private String operatingProfitCurrent;

    /**
     * 유동자산합계
     */
    @SerializedName("currentAssetsTotal")
    private String currentAssetsTotal;

    /**
     * 유동부채합계
     */
    @SerializedName("currentLiabilitiesTotal")
    private String currentLiabilitiesTotal;

    /**
     * 유동비율
     */
    @SerializedName("currentRatio")
    private String currentRatio;

    /**
     * 투자자산(비유동자산내)
     */
    @SerializedName("investmentAssets")
    private String investmentAssets;

    /**
     * 고정부채(비유동부채)
     */
    @SerializedName("fixedLiabilities")
    private String fixedLiabilities;

    /**
     * 발행주식수
     */
    @SerializedName("issuedShares")
    private String issuedShares;
}
