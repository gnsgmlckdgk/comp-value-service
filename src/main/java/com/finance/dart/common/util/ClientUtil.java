package com.finance.dart.common.util;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
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
     * @param body
     * @return
     */
    public static HttpEntity createHttpEntity(MediaType mediaType, Map<String, String> headerDatas, Object body) {

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(mediaType));

        for(String key : headerDatas.keySet()) {
            headers.add(key, headerDatas.get(key));
        }

        // HTTP 엔티티 생성
        HttpEntity<Object> entity = new HttpEntity<>(body, headers);

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
        return addQueryParams(url, paramMap, emptyKeyDelete, true);
    }

    /**
     * URL 쿼리파라미터 추가
     * @param url
     * @param paramDto
     * @param emptyKeyDelete
     * @param urlEncode URL 인코딩 여부
     * @return
     */
    public static String addQueryParams(String url, Object paramDto, boolean emptyKeyDelete, boolean urlEncode) {
        Map<String, String> paramMap = ConvertUtil.toStringMap(ConvertUtil.toMap(paramDto, false), false);
        return addQueryParams(url, paramMap, emptyKeyDelete, urlEncode);
    }

    /**
     * URL 쿼리파라미터 추가
     * @param url
     * @param paramData
     * @param emptyKeyDelete true: 빈 데이터 키 삭제
     * @param urlEncode URL 인코딩 여부
     * @return
     */
    public static String addQueryParams(String url, Map<String, String> paramData, boolean emptyKeyDelete, boolean urlEncode) {

        Map<String, String> newParamData = new LinkedHashMap<>();

        for(String key : paramData.keySet()) {
            String data = paramData.get(key);
            if(emptyKeyDelete && (data == null || "".equals(data))) {
                continue;
            }
            newParamData.put(key, data);
        }

        return addQueryParams(url, newParamData, urlEncode);
    }

    /**
     * URL 쿼리파라미터 추가
     * @param url
     * @param paramData
     * @return
     */
    public static String addQueryParams(String url, Map<String, String> paramData) {
        return addQueryParams(url, paramData, true);
    }

    /**
     * URL 쿼리파라미터 추가
     * @param url
     * @param paramData
     * @param urlEncode URL 인코딩 여부
     * @return
     */
    public static String addQueryParams(String url, Map<String, String> paramData, boolean urlEncode) {

        final String FIRST_DIV = "?";
        final String DIV = "&";

        // URL 동적 치환, {키}
        Map<String, String> workParamData = new LinkedHashMap<>(paramData);
        url = replacePlaceholders(url, workParamData);

        boolean first = true;
        for(String key : workParamData.keySet()) {
            String data = workParamData.get(key);

            // URL 인코딩 처리
            String encodedData = urlEncode ? urlEncode(data) : (data != null ? data : "");

            if(first) {
                url += FIRST_DIV + key + "=" + encodedData;
                first = false;
            } else url += DIV + key + "=" + encodedData;
        }

        return url;
    }

    /**
     * URL 인코딩
     * @param value
     * @return
     */
    private static String urlEncode(String value) {
        if (value == null) {
            return "";
        }
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            // UTF-8은 항상 지원되므로 이 예외는 발생하지 않음
            return value;
        }
    }

    /**
     * <pre>
     * 동적 URL 데이터 변환
     * {키}
     * </pre>
     * @param template
     * @param params
     * @return
     */
    public static String replacePlaceholders(String template, Map<String, String> params) {
        String result = template;

        Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            String placeholder = "{" + entry.getKey() + "}";

            if (result.contains(placeholder)) {
                result = result.replace(placeholder, entry.getValue());
                iterator.remove();
            }
        }

        return result;
    }


}
