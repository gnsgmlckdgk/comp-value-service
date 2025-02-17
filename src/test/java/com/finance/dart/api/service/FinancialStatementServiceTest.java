package com.finance.dart.api.service;

import com.finance.dart.api.dto.FinancialStatementResDTO;
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
public class FinancialStatementServiceTest {

    @Mock
    private HttpClientService httpClientService;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private FinancialStatementService financialStatementService;

    // 더미 API 키 및 응답 JSON 문자열 (실제 FinancialStatementResDTO의 구조에 맞게 수정 필요)
    private final String dummyApiKey = "dummyApiKey";
    private final String dummyResponseJson = "{\"status\":\"000\", \"message\":\"OK\"}";

    @BeforeEach
    void setUp() {
        when(configService.getDartAPI_Key()).thenReturn(dummyApiKey);
        when(httpClientService.exchangeSync(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(dummyResponseJson));
    }

    @Test
    void testGetCompanyFinancialStatement() {
        String corpCode = "001";
        String bsnsYear = "2023";
        String reprtCode = "11011";
        String fsDiv = "CFS";

        FinancialStatementResDTO result = financialStatementService.getCompanyFinancialStatement(corpCode, bsnsYear, reprtCode, fsDiv);
        assertNotNull(result, "응답 DTO는 null이 아니어야 합니다");
        // 예시: FinancialStatementResDTO에 getStatus()가 있다면
        // assertEquals("000", result.getStatus());
    }
}