package com.finance.dart.common.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import java.util.Map;
import java.util.function.Consumer;

/**
 * 비동기 HTTP 요청 정보를 담는 DTO
 * @param <T> 응답 타입
 */
@Getter
@Builder
public class AsyncRequestDto<T> {

    private String id;                                   // 요청 식별자 (응답 매핑용)
    private String url;                                  // 요청 URL
    private HttpMethod method;                           // HTTP 메소드 (GET, POST 등)
    private Map<String, String> headers;                 // 요청 헤더
    private Object body;                                 // 요청 바디
    private ParameterizedTypeReference<T> responseType;  // 응답 타입 (제네릭 지원)
    private Consumer<T> onSuccess;                       // 성공 시 실행할 콜백 함수
    private Consumer<Throwable> onError;                 // 실패 시 실행할 콜백 함수

}
