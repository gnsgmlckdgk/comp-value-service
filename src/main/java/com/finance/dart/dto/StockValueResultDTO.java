package com.finance.dart.dto;

import lombok.Data;

/**
 * 1주당 가치 계산 결과
 */
@Data
public class StockValueResultDTO {

    private String 기업코드;

    private String 기업명;

    private String 주식코드;

    private String 주당가치;    // 1주당 가치

    private StockValueResultDetailDTO 상세정보;

}
