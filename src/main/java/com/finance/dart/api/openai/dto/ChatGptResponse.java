package com.finance.dart.api.openai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;

import java.util.List;


/**
 * OpenAI ChatGPT API 응답 DTO
 * - choices: 생성된 응답 선택지 리스트
 * - created: 응답 생성 시간 (UNIX timestamp)
 * - id: 응답 식별자
 * - model: 사용된 모델 이름
 * - object: 응답 객체 타입
 * - serviceTier: 서비스 티어 (예: "standard", "plus")
 * - usage: 토큰 사용량 정보
 */
@Data
public class ChatGptResponse {

    /** 생성된 응답 선택지 리스트 */
    private List<Choice> choices;

    /** 응답 생성 시간 (UNIX timestamp) */
    private Long created;

    /** 응답 식별자 */
    private String id;

    /** 사용된 모델 이름 (예: "gpt-4") */
    private String model;

    /** 응답 객체 타입 (예: "chat.completion") */
    private String object;

    /** OpenAI 서비스 티어 정보 (예: "standard", "plus") */
    @JsonProperty("service_tier")
    @SerializedName("service_tier")
    private String serviceTier;

    /** 토큰 사용량 정보 */
    private Usage usage;

    /**
     * GPT가 생성한 응답 선택지
     * - finishReason: 응답 종료 사유
     * - index: 응답 인덱스
     * - logprobs: 확률 정보 (선택적)
     * - message: 생성된 메시지
     */
    @Data
    public static class Choice {

        @JsonProperty("finish_reason")
        @SerializedName("finish_reason")
        private String finishReason;

        private Long index;

        private Object logprobs;

        private Message message;
    }

    /**
     * 토큰 사용량 정보
     * - completionTokens: 응답 생성에 사용된 토큰 수
     * - promptTokens: 프롬프트에 사용된 토큰 수
     * - totalTokens: 전체 사용 토큰 수
     * - completionTokensDetails / promptTokensDetails: 세부 토큰 사용 정보
     */
    @Data
    public static class Usage {

        @JsonProperty("completion_tokens")
        @SerializedName("completion_tokens")
        private Long completionTokens;

        @JsonProperty("completion_tokens_details")
        @SerializedName("completion_tokens_details")
        private CompletionTokensDetails completionTokensDetails;

        @JsonProperty("prompt_tokens")
        @SerializedName("prompt_tokens")
        private Long promptTokens;

        @JsonProperty("prompt_tokens_details")
        @SerializedName("prompt_tokens_details")
        private PromptTokensDetails promptTokensDetails;

        @JsonProperty("total_tokens")
        @SerializedName("total_tokens")
        private Long totalTokens;
    }

    /**
     * GPT 메시지 내용
     * - annotations: 응답에 포함된 주석 (선택적)
     * - content: 생성된 텍스트 내용
     * - refusal: 거절 응답 시 정보
     * - role: 발화 주체 ("assistant", "user", "system" 등)
     */
    @Data
    public static class Message {


        private List<Object> annotations;

        private String content;

        private Object refusal;

        private String role;
    }

    /**
     * 응답(Completion)에 사용된 토큰의 세부 정보
     * - acceptedPredictionTokens: 수용된 예측 토큰 수
     * - rejectedPredictionTokens: 거절된 예측 토큰 수
     * - reasoningTokens: 추론 관련 토큰 수
     * - audioTokens: 오디오 처리 토큰 수
     */
    @Data
    public static class CompletionTokensDetails {

        @JsonProperty("accepted_prediction_tokens")
        @SerializedName("accepted_prediction_tokens")
        private Long acceptedPredictionTokens;

        @JsonProperty("audio_tokens")
        @SerializedName("audio_tokens")
        private Long audioTokens;

        @JsonProperty("reasoning_tokens")
        @SerializedName("reasoning_tokens")
        private Long reasoningTokens;

        @JsonProperty("rejected_prediction_tokens")
        @SerializedName("rejected_prediction_tokens")
        private Long rejectedPredictionTokens;
    }

    /**
     * 프롬프트에 사용된 토큰의 세부 정보
     * - cachedTokens: 캐시된 토큰 수
     * - audioTokens: 오디오 처리 토큰 수
     */
    @Data
    public static class PromptTokensDetails {

        @JsonProperty("audio_tokens")
        @SerializedName("audio_tokens")
        private Long audioTokens;

        @JsonProperty("cached_tokens")
        @SerializedName("cached_tokens")
        private Long cachedTokens;
    }

}
