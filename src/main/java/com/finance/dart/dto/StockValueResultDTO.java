package com.finance.dart.dto;

import lombok.Data;

/**
 * 1주당 가치 계산 결과
 */
@Data
public class StockValueResultDTO {

    private String 결과메시지;

    private String 기업코드;

    private String 기업명;

    private String 주식코드;

    private String 주당가치;    // 1주당 가치
    private String 현재가격;    // 현재주식가격
    private String 확인시간;    // yyyyMMdd HH:mm:ss

    private StockValueResultDetailDTO 상세정보;

    public StockValueResultDTO(String responseMessage) {
        this.결과메시지 = responseMessage;
    }
}
