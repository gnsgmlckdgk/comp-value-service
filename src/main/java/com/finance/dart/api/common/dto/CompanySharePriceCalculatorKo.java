package com.finance.dart.api.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 한주당 가치계산 DTO (한글버전)
 * TODO: 개발중
 */
@Data
public class CompanySharePriceCalculatorKo {

    /**
     * 단위
     */
    @SerializedName("unit")
    @JsonProperty("단위")
    private String unit;

    /**
     * 전전기 영업이익
     */
    @SerializedName("operatingProfitPrePre")
    @JsonProperty("전전기영업이익")
    private String operatingProfitPrePre;

    /**
     * 전기 영업이익
     */
    @SerializedName("operatingProfitPre")
    @JsonProperty("전기영업이익")
    private String operatingProfitPre;

    /**
     * 당기 영업이익
     */
    @SerializedName("operatingProfitCurrent")
    @JsonProperty("당기영업이익")
    private String operatingProfitCurrent;

    /**
     * 유동자산합계
     */
    @SerializedName("currentAssetsTotal")
    @JsonProperty("유동자산합계")
    private String currentAssetsTotal;

    /**
     * 유동부채합계
     */
    @SerializedName("currentLiabilitiesTotal")
    @JsonProperty("유동부채합계")
    private String currentLiabilitiesTotal;

    /**
     * 유동비율
     */
    @SerializedName("currentRatio")
    @JsonProperty("유동비율")
    private String currentRatio;

    /**
     * 투자자산(비유동자산내)
     */
    @SerializedName("investmentAssets")
    @JsonProperty("투자자산")
    private String investmentAssets;

    /**
     * 고정부채(비유동부채)
     */
    @SerializedName("fixedLiabilities")
    @JsonProperty("고정부채")
    private String fixedLiabilities;

    /**
     * 발행주식수
     */
    @SerializedName("issuedShares")
    @JsonProperty("발행주식수")
    private String issuedShares;

    /**
     * 현재 주가
     */
    @SerializedName("price")
    private double price = 0;

    /**
     * 기업 CIK (해외기업)
     */
    @SerializedName("cik")
    private String cik;

}
