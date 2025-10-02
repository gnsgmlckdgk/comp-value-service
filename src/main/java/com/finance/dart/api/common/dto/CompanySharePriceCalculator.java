package com.finance.dart.api.common.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

/**
 * 한주당 가치계산 DTO
 */
@Data
public class CompanySharePriceCalculator {

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


    /**
     * PER
     */
    @SerializedName("per")
    private String per;

    /**
     * <pre>
     * 영업이익 성장률
     * v2 추가
     * </pre>
     */
    @SerializedName("growth")
    private String operatingIncomeGrowth;

    /**
     * <pre>
     * 무형자산
     * v2 추가
     * </pre>
     */
    @SerializedName("intangibleAssets")
    private String intangibleAssets;

    /**
     * <pre>
     * 당기 R&D
     * v2 추가
     * </pre>
     */
    @SerializedName("rndCurrent")
    private String rndCurrent;

    /**
     * <pre>
     * 전기 R&D
     * v2 추가
     * </pre>
     */
    @SerializedName("rndPre")
    private String rndPre;

    /**
     * <pre>
     * 전전기 R&D
     * v2 추가
     * </pre>
     */
    @SerializedName("rndPrePre")
    private String rndPrePre;

    /**
     * <pre>
     * 총부채
     * v2 추가
     * </pre>
     */
    @SerializedName("totalDebt")
    private String totalDebt;

    /**
     * <pre>
     * 현금성자산
     * v2 추가
     * </pre>
     */
    @SerializedName("cashAndCashEquivalents")
    private String cashAndCashEquivalents;

    // ---------------------- 필수값 아님

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
