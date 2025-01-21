package com.finance.dart.common.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class HttpClientService {

    private final RestTemplate restTemplate;

    /**
     * 전송[동기]
     * @param url
     * @param method
     * @param entity
     * @param responseType
     * @return
     * @param <T>
     */
    public <T> ResponseEntity<T> exchangeSync(
            String url, HttpMethod method, HttpEntity<?> entity, Class<T> responseType) {

        ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);

        return response;
    }



}
