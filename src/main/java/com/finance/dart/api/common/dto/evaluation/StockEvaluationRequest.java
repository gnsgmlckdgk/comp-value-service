package com.finance.dart.api.common.dto.evaluation;

import lombok.Data;

import java.util.List;

/**
 * 종목 평가 요청 DTO
 */
@Data
public class StockEvaluationRequest {

    /**
     * 평가 대상 심볼 리스트
     */
    private List<String> symbols;

}
