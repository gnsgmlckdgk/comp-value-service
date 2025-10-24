package com.finance.dart.board.dto;

import lombok.Data;

/**
 * 환율정보 갱신 요청 DTO
 */
@Data
public class TranRecordFxRateReqDto {

    private String currency;        // 화폐
}
