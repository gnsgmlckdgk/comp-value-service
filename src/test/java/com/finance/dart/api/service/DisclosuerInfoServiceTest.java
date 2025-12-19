package com.finance.dart.api.service;

import com.finance.dart.api.domestic.dto.DisclosuerInfoReqDTO;
import com.finance.dart.api.domestic.dto.DisclosuerInfoResDTO;
import com.finance.dart.api.domestic.service.DisclosuerInfoService;
import com.finance.dart.common.component.ConfigComponent;
import com.finance.dart.common.component.HttpClientComponent;
import com.google.gson.Gson;
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
public class DisclosuerInfoServiceTest {

    @Mock
    private HttpClientComponent httpClientComponent;

    @Mock
    private ConfigComponent configComponent;

    @InjectMocks
    private DisclosuerInfoService disclosuerInfoService;

    private final Gson gson = new Gson();
    private DisclosuerInfoReqDTO dummyRequest;
    private DisclosuerInfoResDTO dummyResponse;
    private final String dummyApiKey = "dummyApiKey";

    @BeforeEach
    void setUp() {
        // 더미 요청 데이터 (필요한 필드를 채워주세요)
        dummyRequest = new DisclosuerInfoReqDTO();
        // 예: dummyRequest.setSomeField("someValue");

        // 더미 응답 데이터 (필요한 필드를 채워주세요)
        dummyResponse = new DisclosuerInfoResDTO();
        // 예: dummyResponse.setSomeResult("resultValue");

        when(configComponent.getDartApiKey()).thenReturn(dummyApiKey);

        // httpClientService.exchangeSync() 호출 시 dummyResponse를 포함한 ResponseEntity 반환
        when(httpClientComponent.exchangeSync(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(DisclosuerInfoResDTO.class)))
                .thenReturn(ResponseEntity.ok(dummyResponse));
    }

    @Test
    void testGetDisclosuerInfo() {
        DisclosuerInfoResDTO result = disclosuerInfoService.getDisclosuerInfo(dummyRequest);
        assertNotNull(result, "응답 DTO는 null이 아니어야 합니다");
        // 추가 검증: 필요에 따라 dummyResponse와 일치하는지 확인
    }
}