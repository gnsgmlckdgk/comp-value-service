package com.finance.dart.api.domestic.service;

import com.finance.dart.api.domestic.dto.DisclosuerInfoReqDTO;
import com.finance.dart.api.domestic.dto.DisclosuerInfoResDTO;
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

import java.util.Map;

/**
 * 공시정보 > 공시목록 조회 서비스
 */
@Slf4j
@Service
@AllArgsConstructor
public class DisclosuerInfoService {

    private final HttpClientService httpClientService;
    private final ConfigService configService;
    private final Gson gson = new Gson();

    /**
     * 공시정보 목록을 조회한다.
     *
     * @param request 공시정보 요청 DTO
     * @return 공시정보 응답 DTO
     */
    public DisclosuerInfoResDTO getDisclosuerInfo(DisclosuerInfoReqDTO request) {
        final String apiKey = configService.getDartApiKey();
        final HttpEntity<?> httpEntity = ClientUtil.createHttpEntity(MediaType.APPLICATION_JSON);
        String url = "https://opendart.fss.or.kr/api/list.json";

        // 요청 DTO를 Map으로 변환하여 query parameter 구성
        Map<String, String> queryParams = gson.fromJson(gson.toJson(request), Map.class);
        queryParams.put("crtfc_key", apiKey);

        url = ClientUtil.addQueryParams(url, queryParams, true);
        ResponseEntity<DisclosuerInfoResDTO> response = httpClientService.exchangeSync(
                url,
                HttpMethod.GET,
                httpEntity,
                DisclosuerInfoResDTO.class
        );

        return response.getBody();
    }
}