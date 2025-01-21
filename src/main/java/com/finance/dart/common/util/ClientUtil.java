package com.finance.dart.common.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.Map;

public class ClientUtil {

    /**
     * HttpEntity 생성
     * TODO: 요청헤더추가기능
     * @param mediaType
     * @return
     */
    public static HttpEntity createHttpEntity(MediaType mediaType) {

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(mediaType));

        // HTTP 엔티티 생성
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return entity;
    }

    /**
     * URL 쿼리파라미터 추가
     * @param url
     * @param paramData
     * @return
     */
    public static String addQueryParams(String url, Map<String, String> paramData) {

        final String FIRST_DIV = "?";
        final String DIV = "&";

        boolean first = true;
        for(String key : paramData.keySet()) {
            String data = paramData.get(key);

            if(first) {
                url += FIRST_DIV + key + "=" + data;
                first = false;
            } else url += DIV + key + "=" + data;
        }

        return url;
    }

}
