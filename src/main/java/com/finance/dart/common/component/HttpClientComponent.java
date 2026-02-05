package com.finance.dart.common.component;

import com.finance.dart.common.dto.AsyncRequestDto;
import com.finance.dart.common.util.ClientUtil;
import com.finance.dart.common.util.DateUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class HttpClientComponent {

    private final RestTemplate restTemplate;
    private final WebClient webClient;

    /**
     * 커스텀 타임아웃으로 동기 호출
     * @param url
     * @param method
     * @param httpHeaders
     * @param body
     * @param responseType
     * @param readTimeoutMs 읽기 타임아웃 (밀리초)
     * @return
     * @param <T>
     */
    public <T> ResponseEntity<T> exchangeSyncWithTimeout(
            String url, HttpMethod method, Map<String, String> httpHeaders,
            Object body, ParameterizedTypeReference<T> responseType,
            int readTimeoutMs) {

        if (httpHeaders == null) httpHeaders = new LinkedHashMap<>();

        // 임시 RestTemplate 생성 (커스텀 타임아웃)
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(readTimeoutMs);
        RestTemplate customRestTemplate = new RestTemplate(factory);

        final HttpEntity<?> httpEntity = ClientUtil.createHttpEntity(MediaType.APPLICATION_JSON, httpHeaders, body);

        if (log.isDebugEnabled()) {
            log.debug("전송 요청 정보 (커스텀 타임아웃 {}ms) = url[{}], method[{}], entity[{}], responseType[{}]",
                    readTimeoutMs, url, method, httpEntity, responseType);
        }

        long startTime = DateUtil.getCurrentNanoTime();

        ResponseEntity<T> response = customRestTemplate.exchange(url, method, httpEntity, responseType);

        long elapsedTime = DateUtil.getElapsedTimeMillis(startTime);
        if (log.isDebugEnabled()) {
            log.debug("API 호출 완료 - URL: {}, Method: {}, 응답시간: {}ms, 상태코드: {}",
                    url, method, elapsedTime, response.getStatusCode());
        }

        return response;
    }

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

        long startTime = DateUtil.getCurrentNanoTime();

        ResponseEntity<T> response = restTemplate.exchange(url, method, entity, responseType);

        long elapsedTime = DateUtil.getElapsedTimeMillis(startTime);
        if(log.isDebugEnabled()) {
            log.debug("API 호출 완료 - URL: {}, Method: {}, 응답시간: {}ms, 상태코드: {}",
                url, method, elapsedTime, response.getStatusCode());
        }

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
        if(httpHeaders == null) httpHeaders = new LinkedHashMap<>();

        final HttpEntity<?> httpEntity = ClientUtil.createHttpEntity(MediaType.APPLICATION_JSON, httpHeaders, body);

        if(log.isDebugEnabled()) log.debug("전송 요청 정보 = url[{}], method[{}], entity[{}], responseType[{}]", url, method, httpEntity, responseType);

        long startTime = DateUtil.getCurrentNanoTime();

        ResponseEntity<T> response = restTemplate.exchange(url, method, httpEntity, responseType);

        long elapsedTime = DateUtil.getElapsedTimeMillis(startTime);
        if(log.isDebugEnabled()) {
            log.debug("API 호출 완료 - URL: {}, Method: {}, 응답시간: {}ms, 상태코드: {}",
                url, method, elapsedTime, response.getStatusCode());
        }

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

        if(log.isDebugEnabled()) {
            log.debug("API 호출 시작 - URL: {}, Method: {}", url, method);
        }

        long startTime = DateUtil.getCurrentNanoTime();

        // URL이 이미 인코딩되어 있으므로 URI.create()를 사용하여 이중 인코딩 방지
        URI uri = URI.create(url);
        ResponseEntity<T> response = restTemplate.exchange(uri, method, httpEntity, responseType);

        long elapsedTime = DateUtil.getElapsedTimeMillis(startTime);
        if(log.isDebugEnabled()) {
            log.debug("API 호출 완료 - URI: {}, Method: {}, 응답시간: {}ms, 상태코드: {}, 응답바디: {}",
                uri, method, elapsedTime, response.getStatusCode(), response.getBody());
        }

        return response;
    }

    /**
     * 병렬 일괄 전송 (동기 블로킹) - 응답 결과 Map 반환
     * 모든 요청을 동시에 보내고, 전부 완료될 때까지 대기 후 id-응답 Map 반환
     * @param requests 요청 정보 리스트 (id 필수)
     * @return id를 키로 하는 응답 결과 Map (실패한 요청은 포함되지 않음)
     */
    public <R> Map<String, R> exchangeParallel(List<AsyncRequestDto<R>> requests) {
        long startTime = DateUtil.getCurrentNanoTime();

        Map<String, R> results = Flux.fromIterable(requests)
                .flatMap(req -> processSingleRequestWithResponse(req)
                        .map(response -> Map.entry(req.getId(), response)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue)
                .block();

        long elapsedTime = DateUtil.getElapsedTimeMillis(startTime);
        if (log.isDebugEnabled()) {
            log.debug("병렬 API 호출 전체 완료 - 요청 수: {}, 성공 수: {}, 총 소요시간: {}ms",
                    requests.size(), results != null ? results.size() : 0, elapsedTime);
        }

        return results != null ? results : new HashMap<>();
    }

    /**
     * 비동기 일괄 전송 (콜백 방식)
     * @param requests 요청 정보 리스트
     * @return Mono<Void> 모든 요청이 완료되면 완료되는 Mono
     */
    public <T> Mono<Void> exchangeAsync(List<AsyncRequestDto<T>> requests) {
        List<Mono<Void>> monos = requests.stream()
                .map(this::processSingleRequestWithCallback)
                .collect(Collectors.toList());

        return Mono.when(monos);
    }

    /**
     * 단일 요청 처리 - 응답 반환용
     */
    private <T> Mono<T> processSingleRequestWithResponse(AsyncRequestDto<T> request) {
        long startTime = DateUtil.getCurrentNanoTime();
        ParameterizedTypeReference<T> responseType = request.getResponseType();

        WebClient.RequestBodySpec requestSpec = webClient
                .method(request.getMethod())
                .uri(URI.create(request.getUrl()))
                .headers(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    if (request.getHeaders() != null) {
                        request.getHeaders().forEach(headers::add);
                    }
                });

        WebClient.RequestHeadersSpec<?> headersSpec;
        if (request.getBody() != null) {
            headersSpec = requestSpec.bodyValue(request.getBody());
        } else {
            headersSpec = requestSpec;
        }

        return headersSpec
                .retrieve()
                .bodyToMono(responseType)
                .doOnSuccess(response -> {
                    long elapsedTime = DateUtil.getElapsedTimeMillis(startTime);
                    if (log.isDebugEnabled()) {
                        log.debug("병렬 API 호출 완료 - URL: {}, Method: {}, 응답시간: {}ms",
                                request.getUrl(), request.getMethod(), elapsedTime);
                    }
                })
                .doOnError(error -> {
                    long elapsedTime = DateUtil.getElapsedTimeMillis(startTime);
                    log.error("병렬 API 호출 실패 - URL: {}, Method: {}, 응답시간: {}ms, 에러: {}",
                            request.getUrl(), request.getMethod(), elapsedTime, error.getMessage());
                })
                .onErrorResume(e -> Mono.empty());
    }

    /**
     * 단일 요청 처리 - 콜백 방식용
     */
    @SuppressWarnings("unchecked")
    private <T> Mono<Void> processSingleRequestWithCallback(AsyncRequestDto<?> request) {
        long startTime = DateUtil.getCurrentNanoTime();

        AsyncRequestDto<T> typedRequest = (AsyncRequestDto<T>) request;
        ParameterizedTypeReference<T> responseType = typedRequest.getResponseType();

        WebClient.RequestBodySpec requestSpec = webClient
                .method(typedRequest.getMethod())
                .uri(URI.create(typedRequest.getUrl()))
                .headers(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    if (typedRequest.getHeaders() != null) {
                        typedRequest.getHeaders().forEach(headers::add);
                    }
                });

        WebClient.RequestHeadersSpec<?> headersSpec;
        if (typedRequest.getBody() != null) {
            headersSpec = requestSpec.bodyValue(typedRequest.getBody());
        } else {
            headersSpec = requestSpec;
        }

        return headersSpec
                .retrieve()
                .bodyToMono(responseType)
                .doOnSuccess(response -> {
                    long elapsedTime = DateUtil.getElapsedTimeMillis(startTime);
                    if (log.isDebugEnabled()) {
                        log.debug("비동기 API 호출 완료 - URL: {}, Method: {}, 응답시간: {}ms",
                                typedRequest.getUrl(), typedRequest.getMethod(), elapsedTime);
                    }
                    if (typedRequest.getOnSuccess() != null) {
                        typedRequest.getOnSuccess().accept(response);
                    }
                })
                .doOnError(error -> {
                    long elapsedTime = DateUtil.getElapsedTimeMillis(startTime);
                    log.error("비동기 API 호출 실패 - URL: {}, Method: {}, 응답시간: {}ms, 에러: {}",
                            typedRequest.getUrl(), typedRequest.getMethod(), elapsedTime, error.getMessage());
                    if (typedRequest.getOnError() != null) {
                        typedRequest.getOnError().accept(error);
                    }
                })
                .onErrorResume(e -> Mono.empty())
                .then();
    }

}
