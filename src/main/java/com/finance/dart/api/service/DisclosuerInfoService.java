package com.finance.dart.api.service;

import com.finance.dart.api.dto.DisclosuerInfoReqDTO;
import com.finance.dart.common.service.ConfigService;
import com.finance.dart.common.service.HttpClientService;
import com.finance.dart.common.util.ClientUtil;
import com.finance.dart.api.dto.DisclosuerInfoResDTO;
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
 * 공시정보 > 공시목록 조회
 */

@Slf4j
@Service
@AllArgsConstructor
public class DisclosuerInfoService {

    private final HttpClientService httpClientService;
    private final ConfigService configService;


    /**
     * 공시정보 목록 조회
     * @return
     */
    public DisclosuerInfoResDTO getDisclosuerInfo(DisclosuerInfoReqDTO disclosuerInfoReqDTO) {

        final String API_KEY = configService.getDartAPI_Key();

        HttpEntity entity = ClientUtil.createHttpEntity(MediaType.APPLICATION_JSON);
        String url = "https://opendart.fss.or.kr/api/list.json";

        Map<String, String> paramData = new Gson().fromJson(new Gson().toJson(disclosuerInfoReqDTO), Map.class);
        paramData.put("crtfc_key", API_KEY);    // API인증키

        url = ClientUtil.addQueryParams(url, paramData, true);
        ResponseEntity<DisclosuerInfoResDTO> response = httpClientService.exchangeSync(url, HttpMethod.GET, entity, DisclosuerInfoResDTO.class);

        return response.getBody();
    }



}
