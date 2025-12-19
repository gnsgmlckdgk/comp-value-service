package com.finance.dart.api.domestic.service;

import com.finance.dart.api.domestic.dto.FinancialStatementResDTO;
import com.finance.dart.common.component.ConfigComponent;
import com.finance.dart.common.component.HttpClientComponent;
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
 * 재무제표 조회 서비스
 */
@Slf4j
@Service
@AllArgsConstructor
public class FinancialStatementService {

    private final HttpClientComponent httpClientComponent;
    private final ConfigComponent configComponent;
    private final Gson gson = new Gson();

    /**
     * 재무제표를 조회하여 FinancialStatementResDTO 객체로 반환한다.
     *
     * @param corpCode  기업 고유번호
     * @param bsnsYear  사업 연도
     * @param reprtCode 보고서 코드
     * @param fsDiv     개별/연결 구분
     * @return FinancialStatementResDTO 객체
     */
    public FinancialStatementResDTO getCompanyFinancialStatement(String corpCode, String bsnsYear, String reprtCode, String fsDiv) {
        final String apiKey = configComponent.getDartApiKey();
        HttpEntity<?> entity = ClientUtil.createHttpEntity(MediaType.APPLICATION_JSON);
        String url = "https://opendart.fss.or.kr/api/fnlttSinglAcntAll.json";

        Map<String, String> params = new LinkedHashMap<>();
        params.put("crtfc_key", apiKey);
        params.put("corp_code", corpCode);
        params.put("bsns_year", bsnsYear);
        params.put("reprt_code", reprtCode);
        params.put("fs_div", fsDiv);

        url = ClientUtil.addQueryParams(url, params);
        if(log.isDebugEnabled()) log.debug("Request URL: {}", url);

        ResponseEntity<String> response = httpClientComponent.exchangeSync(url, HttpMethod.GET, entity, String.class);
        String responseBody = response.getBody();
        if(log.isDebugEnabled()) log.debug("Response Body: {}", responseBody);

        return gson.fromJson(responseBody, FinancialStatementResDTO.class);
    }
}