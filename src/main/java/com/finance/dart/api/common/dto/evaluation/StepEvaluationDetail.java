package com.finance.dart.api.common.dto.evaluation;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Step별 평가 상세 정보
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonPropertyOrder({"stepNumber", "stepName", "score", "maxScore", "description", "details"})
public class StepEvaluationDetail {

    /**
     * 스텝 번호
     */
    private int stepNumber;

    /**
     * 스텝 이름
     */
    private String stepName;

    /**
     * 획득 점수
     */
    private double score;

    /**
     * 만점 점수
     */
    private int maxScore;

    /**
     * 스텝 설명
     */
    private String description;

    /**
     * 평가 상세 내용
     */
    private String details;

}
