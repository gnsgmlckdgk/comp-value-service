package com.finance.dart.api.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

/**
 * OpenAI ChatGPT API 요청 DTO
 * model: 사용할 GPT 모델명 (예: "gpt-3.5-turbo", "gpt-4", "gpt-5")
 * messages: 사용자와 시스템 간의 대화 메시지 목록
 */
@Data
@AllArgsConstructor
public class ChatGptRequest {

    /** 사용할 GPT 모델명 (예: "gpt-3.5-turbo", "gpt-4", "gpt-5") */
    private String model;
    /** 사용자와 시스템 간의 대화 메시지 목록 */
    private List<Message> messages;

    /**
     * ChatGPT 메시지 객체
     * role: 메시지의 발신자 역할 ("system", "user", "assistant" 중 하나)
     * content: 메시지 내용
     */
    @Data
    @AllArgsConstructor
    public static class Message {
        /** 메시지의 발신자 역할 ("system", "user", "assistant" 중 하나) */
        private String role;
        /** 메시지 내용 */
        private String content;
    }
}
