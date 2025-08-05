package com.finance.dart.api.abroad.dto;

import lombok.Data;

@Data
public class FindCompanySymbolResDto {

    private String symbol;
    private String name;
    private String currency;
    private String exchangeFullName;
    private String exchange;

}
