package com.finance.dart.api.domestic.service;


import com.finance.dart.common.component.ConfigComponent;
import com.finance.dart.common.component.HttpClientComponent;
import com.finance.dart.common.util.ClientUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * 기업목록 조회 캐싱
 */
@Slf4j
@Service
@AllArgsConstructor
public class CorpCodeCacheService {

    private final HttpClientComponent httpClientComponent;
    private final ConfigComponent configComponent;

    /**
     * 기업목록 조회(OpenDart File 응답)
     * @return
     */
    @Cacheable(value = "corpCodeFile", key = "'corpCodeFile'")
    public ResponseEntity<byte[]> getCoprCodeFile() {

        if(log.isDebugEnabled()) log.debug("📡 캐시에 없음 → 외부 API 호출 [corpCodeFile]");

        final String apiKey = configComponent.getDartApiKey();
        final HttpEntity<?> entity = ClientUtil.createHttpEntity(MediaType.APPLICATION_XML);
        final String url = "https://opendart.fss.or.kr/api/corpCode.xml?crtfc_key=" + apiKey;

        return httpClientComponent.exchangeSync(url, HttpMethod.GET, entity, byte[].class);
    }

}
