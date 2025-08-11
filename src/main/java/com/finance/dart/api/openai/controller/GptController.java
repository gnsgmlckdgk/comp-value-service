package com.finance.dart.api.openai.controller;

import com.finance.dart.api.openai.service.OpenAiService;
import com.finance.dart.common.dto.CommonResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * GPT 컨틀로러
 */
@AllArgsConstructor
@RestController
public class GptController {

    private final OpenAiService openAiService;

    /**
     * 한글 기업명을 영어 기업명으로 변환 조회
     * @param koreanCompanyName
     * @return
     */
    @GetMapping("/gpt/translate/company-name/eng")
    public ResponseEntity<CommonResponse<String>> translateCompanyNameToEng(@RequestParam(name = "kor") String koreanCompanyName) {

        // TODO: 라이센스 없어서 추후 진행예정

        String prompt = String.format("다음 한글 기업명을 영어 공식 기업명으로 번역해주고 기업명만 대답해줘(예로 '마이크로소프트' 이면 'Microsoft 만): \"%s\"", koreanCompanyName);
        String response = openAiService.askGpt(prompt);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }


}
