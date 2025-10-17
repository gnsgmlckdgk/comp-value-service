package com.finance.dart.api.abroad.dto.fmp.company;

import lombok.Data;

/**
 * CompanyStockSymbolSearch / StockSymbolSearch API 응답
 */
@Data
public class FindCompanySymbolResDto {

    private String symbol;
    private String name;
    private String currency;
    private String exchangeFullName;
    private String exchange;

}
