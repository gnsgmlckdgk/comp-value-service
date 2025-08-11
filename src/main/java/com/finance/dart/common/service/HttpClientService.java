package com.finance.dart.common.service;

import com.finance.dart.common.util.ClientUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
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

    /**
     * 전송[동기]
     * @param url
     * @param method
     * @param httpHeaders
     * @param body
     * @param responseType
     * @return
     * @param <T>
     */
    public <T> ResponseEntity<T> exchangeSync(
            String url, HttpMethod method, Map<String ,String> httpHeaders, Object body, ParameterizedTypeReference<T> responseType
    ) {
        final HttpEntity<?> httpEntity = ClientUtil.createHttpEntity(MediaType.APPLICATION_JSON, httpHeaders, body);

        if(log.isDebugEnabled()) log.debug("전송 요청 정보 = url[{}], method[{}], entity[{}], responseType[{}]", url, method, httpEntity, responseType);
        ResponseEntity<T> response = restTemplate.exchange(url, method, httpEntity, responseType);

        return response;
    }

    /**
     * 전송[동기]
     * @param url
     * @param method
     * @param responseType
     * @return
     * @param <T>
     */
    public <T> ResponseEntity<T> exchangeSync(
            String url, HttpMethod method, ParameterizedTypeReference<T> responseType
    ) {
        final HttpEntity<?> httpEntity = ClientUtil.createHttpEntity(MediaType.APPLICATION_JSON);
        ResponseEntity<T> response = restTemplate.exchange(url, method, httpEntity, responseType);

        return response;
    }


}
