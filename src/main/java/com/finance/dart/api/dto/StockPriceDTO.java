package com.finance.dart.api.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

@Getter
public class StockPriceDTO {
    @SerializedName("chart")
    private Chart chart;

    @Getter
    public static class Chart {
        @SerializedName("result")
        private List<Result> result;
    }

    @Getter
    public static class Result {
        @SerializedName("meta")
        private Meta meta;
    }

    @Getter
    public static class Meta {
        @SerializedName("instrumentType")
        private String instrumentType;
        @SerializedName("exchangeName")
        private String exchangeName;
        @SerializedName("fullExchangeName")
        private String fullExchangeName;
        @SerializedName("regularMarketPrice")
        private Double regularMarketPrice;
        @SerializedName("symbol")
        private String symbol;
    }
}
