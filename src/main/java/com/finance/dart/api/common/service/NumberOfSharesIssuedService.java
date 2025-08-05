package com.finance.dart.api.common.service;

import com.finance.dart.api.domestic.dto.NumberOfSharesIssuedResDTO;
import com.finance.dart.common.service.ConfigService;
import com.finance.dart.common.service.HttpClientService;
import com.finance.dart.common.util.ClientUtil;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 주식 총수 현황 조회 서비스
 */
@Slf4j
@Service
@AllArgsConstructor
public class NumberOfSharesIssuedService {

    private final HttpClientService httpClientService;
    private final ConfigService configService;
    private final Gson gson = new Gson();

    /**
     * 주식의 총수 현황을 조회하여 NumberOfSharesIssuedResDTO 객체로 반환한다.
     *
     * @param corpCode  기업 고유번호
     * @param bsnsYear  사업 연도
     * @param reprtCode 보고서 코드
     * @return NumberOfSharesIssuedResDTO 객체
     */
    public NumberOfSharesIssuedResDTO getNumberOfSharesIssued(String corpCode, String bsnsYear, String reprtCode) {
        final String apiKey = configService.getDartAPI_Key();
        final HttpEntity<?> httpEntity = ClientUtil.createHttpEntity(MediaType.APPLICATION_JSON);
        String url = "https://opendart.fss.or.kr/api/stockTotqySttus.json";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("crtfc_key", apiKey);
        params.put("corp_code", corpCode);
        params.put("bsns_year", bsnsYear);
        params.put("reprt_code", reprtCode);

        url = ClientUtil.addQueryParams(url, params);
        log.debug("Request URL: {}", url);

        ResponseEntity<String> response = httpClientService.exchangeSync(url, HttpMethod.GET, httpEntity, String.class);
        String responseBody = response.getBody();
        log.debug("Response Body: {}", responseBody);

        return gson.fromJson(responseBody, NumberOfSharesIssuedResDTO.class);
    }
}