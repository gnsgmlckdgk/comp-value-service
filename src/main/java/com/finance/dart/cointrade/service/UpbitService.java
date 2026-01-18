package com.finance.dart.cointrade.service;

import com.finance.dart.cointrade.dto.upbit.TradingParisDto;
import com.finance.dart.common.component.ConfigComponent;
import com.finance.dart.common.component.HttpClientComponent;
import com.finance.dart.common.util.ConvertUtil;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpbitService {

    private final ConfigComponent configComponent;
    private final HttpClientComponent httpClientComponent;


    @Transactional
    public List<TradingParisDto> getTradingPairs() {

        List<TradingParisDto> response = null;

        String url = "https://api.upbit.com/v1/market/all";
        HttpMethod method = HttpMethod.GET;

        Object sendResponse = upbitSendSimple(url, method, null, null);
        if(sendResponse != null) {
            response = ConvertUtil.parseObject(sendResponse, new TypeToken<List<TradingParisDto>>() {});
        }

        return response;
    }


    /**
     * 업비트 거래전송
     * @param url
     * @param method
     * @param httpHeaders Httpheader 세팅 (없으면 null)
     * @param body 요청 바디데이터 (없으면 null)
     * @return
     */
    private Object upbitSendSimple(String url, HttpMethod method, Map<String, String> httpHeaders, Object body) {

        if(httpHeaders == null) httpHeaders = new HashMap<>();

        try {
            ResponseEntity<Object> response = httpClientComponent.exchangeSync(url, method, httpHeaders, body, new ParameterizedTypeReference<Object>() {});
            return response.getBody();

        } catch (Exception e) {
            return null;
        }

    }

}
