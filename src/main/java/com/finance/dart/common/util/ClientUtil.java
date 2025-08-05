package com.finance.dart.common.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ClientUtil {

    /**
     * HttpEntity 생성
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
     * HttpEntity 생성
     * @param mediaType
     * @param headerDatas
     * @return
     */
    public static HttpEntity createHttpEntity(MediaType mediaType, Map<String, String> headerDatas) {

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(mediaType));

        for(String key : headerDatas.keySet()) {
            headers.add(key, headerDatas.get(key));
        }


        // HTTP 엔티티 생성
        HttpEntity<String> entity = new HttpEntity<>(headers);

        return entity;
    }

    /**
     * URL 쿼리파라미터 추가
     * @param url
     * @param paramDto
     * @param emptyKeyDelete
     * @return
     */
    public static String addQueryParams(String url, Object paramDto, boolean emptyKeyDelete) {
        Map<String, String> paramMap = ConvertUtil.toStringMap(ConvertUtil.toMap(paramDto, false), false);
        return addQueryParams(url, paramMap, emptyKeyDelete);
    }

    /**
     * URL 쿼리파라미터 추가
     * @param url
     * @param paramData
     * @param emptyKeyDelete true: 빈 데이터 키 삭제
     * @return
     */
    public static String addQueryParams(String url, Map<String, String> paramData, boolean emptyKeyDelete) {

        Map<String, String> newParamData = new LinkedHashMap<>();

        for(String key : paramData.keySet()) {
            String data = paramData.get(key);
            if(emptyKeyDelete && (data == null || "".equals(data))) {
                continue;
            }
            newParamData.put(key, data);
        }

        return addQueryParams(url, newParamData);
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
