package com.finance.dart.api.abroad.dto.fmp.codelist;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ExchangesDto {
    private String exchange;
    private String name;
    private String countryName;
    private String countryCode;
    private String symbolSuffix;
    private String delay;
}
