package com.finance.dart.board.dto;

import lombok.Data;

import java.util.List;

/**
 * 현재가 갱신 요청 DTO
 */
@Data
public class TranRecordCurValueReqDto {

    private List<String> symbols; // 티커

}
