package com.finance.dart.service;


import com.finance.dart.common.service.ConfigService;
import com.finance.dart.common.service.HttpClientService;
import com.finance.dart.common.util.ClientUtil;
import com.finance.dart.dto.NumberOfSharesIssuedResDTO;
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

@Slf4j
@Service
@AllArgsConstructor
public class NumberOfSharesIssuedService {

    private final HttpClientService httpClientService;
    private final ConfigService configService;

    /**
     * 주식의 총수 현황 조회
     * @param corpCode  고유번호
     * @param bsnsYear  사업연도
     * @param reprtCode 보고서코드
     * @return
     */
    public NumberOfSharesIssuedResDTO getNumberOfSharesIssued(String corpCode, String bsnsYear, String reprtCode) {

        final String API_KEY = configService.getDartAPI_Key();

        HttpEntity entity = ClientUtil.createHttpEntity(MediaType.APPLICATION_JSON);
        String url = "https://opendart.fss.or.kr/api/stockTotqySttus.json";

        Map<String, String> paramData = new LinkedHashMap<>();
        paramData.put("crtfc_key", API_KEY);
        paramData.put("corp_code", corpCode);
        paramData.put("bsns_year", bsnsYear);
        paramData.put("reprt_code", reprtCode);

        url = ClientUtil.addQueryParams(url, paramData);

        ResponseEntity<String> response = httpClientService.exchangeSync(url, HttpMethod.GET, entity, String.class);
        NumberOfSharesIssuedResDTO numberOfSharesIssuedResDTO =
                new Gson().fromJson(response.getBody(), NumberOfSharesIssuedResDTO.class);

        return numberOfSharesIssuedResDTO;
    }


}
