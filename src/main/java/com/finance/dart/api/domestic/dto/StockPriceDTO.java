package com.finance.dart.api.domestic.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StockPriceDTO {
    @SerializedName("chart")
    private Chart chart;

    @Getter
    @Setter
    public static class Chart {
        @SerializedName("result")
        private List<Result> result;
    }

    @Getter
    @Setter
    public static class Result {
        @SerializedName("meta")
        private Meta meta;
    }

    @Getter
    @Setter
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
