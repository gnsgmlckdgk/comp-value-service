package com.finance.dart.api.service;


import com.finance.dart.api.dto.FinancialStatementResDTO;
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

@Slf4j
@Service
@AllArgsConstructor
public class FinancialStatmentService {

    private final HttpClientService httpClientService;
    private final ConfigService configService;

    /**
     * 재무제표 조회
     * @param corpCode  고유번호
     * @param bsnsYear  사업연도
     * @param reprtCode 보고서코드
     * @param fsDiv     개별/연결구분
     * @return
     */
    public FinancialStatementResDTO getCompanyFinancialStatement(String corpCode, String bsnsYear, String reprtCode, String fsDiv) {

        final String API_KEY = configService.getDartAPI_Key();

        HttpEntity entity = ClientUtil.createHttpEntity(MediaType.APPLICATION_JSON);
        String url = "https://opendart.fss.or.kr/api/fnlttSinglAcntAll.json";

        Map<String, String> paramData = new LinkedHashMap<>();
        paramData.put("crtfc_key", API_KEY);
        paramData.put("corp_code", corpCode);
        paramData.put("bsns_year", bsnsYear);
        paramData.put("reprt_code", reprtCode);
        paramData.put("fs_div", fsDiv);

        url = ClientUtil.addQueryParams(url, paramData);

        ResponseEntity<String> response = httpClientService.exchangeSync(url, HttpMethod.GET, entity, String.class);
        FinancialStatementResDTO financialStatementResDTO =
                new Gson().fromJson(response.getBody(), FinancialStatementResDTO.class);

        return financialStatementResDTO;
    }


}
