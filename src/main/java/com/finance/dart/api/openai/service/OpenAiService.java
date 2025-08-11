package com.finance.dart.api.openai.service;

import com.finance.dart.api.openai.dto.ChatGptRequest;
import com.finance.dart.api.openai.dto.ChatGptResponse;
import com.finance.dart.common.service.ConfigService;
import com.finance.dart.common.service.HttpClientService;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@AllArgsConstructor
@Service
public class OpenAiService {

    private final ConfigService configService;
    private final HttpClientService clientService;


    private static final String GPT_MODEL = "gpt-3.5-turbo";
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    /**
     * GPT에 질문
     * @param prompt
     * @return
     */
    public String askGpt(String prompt) {

        //@ 요청 세팅
        final String API_KEY = configService.getOpenAiApiKey(); // OpenAI API 키

        // 요청헤더
        Map<String, String> httpHeaders = new LinkedHashMap<>();
        httpHeaders.put("Content-Type", "application/json");
        httpHeaders.put("Authorization", "Bearer " + API_KEY);

        // 요청바디
        ChatGptRequest.Message message = new ChatGptRequest.Message("user", prompt);
        ChatGptRequest chatGptRequest = new ChatGptRequest(GPT_MODEL, Arrays.asList(message));

        //@ 전송
        ResponseEntity<ChatGptResponse> response =
                clientService.exchangeSync(API_URL, HttpMethod.POST, httpHeaders, chatGptRequest, new ParameterizedTypeReference<>() {});

        //@ 응답
        ChatGptResponse chatGptResponse = response.getBody();
        String responseMessage = chatGptResponse.getChoices().get(0).getMessage().getContent();

        return responseMessage;
    }
}
