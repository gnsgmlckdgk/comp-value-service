package com.finance.dart.api.service;

import com.finance.dart.api.common.service.NumberOfSharesIssuedService;
import com.finance.dart.api.domestic.dto.NumberOfSharesIssuedResDTO;
import com.finance.dart.common.service.ConfigService;
import com.finance.dart.common.service.HttpClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NumberOfSharesIssuedServiceTest {

    @Mock
    private HttpClientService httpClientService;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private NumberOfSharesIssuedService numberOfSharesIssuedService;

    // 더미 API 키 및 응답 JSON 문자열
    private final String dummyApiKey = "dummyApiKey";
    // 예시 JSON: 실제 DTO의 구조에 맞게 조정해야 합니다.
    // 예: {"status": "000", "list": [{"se": "보통주", "istc_totqy": "100"}]}
    private final String dummyResponseJson = "{\"status\":\"000\", \"list\":[{\"se\":\"보통주\", \"istc_totqy\":\"100\"}]}";

    @BeforeEach
    void setUp() {
        when(configService.getDartApiKey()).thenReturn(dummyApiKey);
        when(httpClientService.exchangeSync(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(dummyResponseJson));
    }

    @Test
    void testGetNumberOfSharesIssued() {
        String corpCode = "001";
        String bsnsYear = "2023";
        String reprtCode = "11011";

        NumberOfSharesIssuedResDTO result = numberOfSharesIssuedService.getNumberOfSharesIssued(corpCode, bsnsYear, reprtCode);
        assertNotNull(result, "응답 DTO는 null이 아니어야 합니다");
        // 예시: DTO에 getStatus()와 getList() 메서드가 있다면
        // assertEquals("000", result.getStatus());
        // assertNotNull(result.getList());
        // assertFalse(result.getList().isEmpty());
        // assertEquals("보통주", result.getList().get(0).getSe());
        // assertEquals("100", result.getList().get(0).getIstc_totqy());
    }
}