package com.finance.dart.api.abroad.component;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import com.finance.dart.api.abroad.enums.FmpApiList;
import com.finance.dart.common.component.ConfigComponent;
import com.finance.dart.common.component.HttpClientComponent;
import com.finance.dart.common.dto.AsyncRequestDto;
import com.finance.dart.common.util.ClientUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@AllArgsConstructor
@Component
public class FmpClientComponent {

    private final ConfigComponent configComponent;
    private final HttpClientComponent httpClientComponent;

    /**
     * FMP API GET 전송
     * @param requestData
     * @return
     */
    public <T extends FmpReqCommon, R> R sendGet(FmpApiList fmpApi, T requestData, ParameterizedTypeReference<R> responseType) {

        //@ 요청 데이터
        requestData.setApikey(configComponent.getFmpApiKey());

        String url = fmpApi.url;
        url = ClientUtil.addQueryParams(url, requestData, true);

        //@ 요청
        ResponseEntity<R> response =
                httpClientComponent.exchangeSync(url, HttpMethod.GET, responseType);

        return response.getBody();
    }

    /**
     * FMP API GET 병렬 전송(타겟은 한 곳)
     * @param fmpApi API 정보
     * @param requestDataList 요청 데이터 리스트
     * @param idExtractor 요청 데이터에서 식별자 추출 함수 (예: RatiosTtmReqDto::getSymbol)
     * @param responseType 응답 타입
     * @return 식별자를 키로 하는 응답 Map
     * @param <T> 요청타입
     * @param <R> 응답타입
     */
    public <T extends FmpReqCommon, R> Map<String, R> sendGetParallel(
            FmpApiList fmpApi,
            List<T> requestDataList,
            Function<T, String> idExtractor,
            ParameterizedTypeReference<R> responseType) {

        List<AsyncRequestDto<R>> sendBodyList = new LinkedList<>();

        for (T reqData : requestDataList) {

            //@ API KEY 세팅
            reqData.setApikey(configComponent.getFmpApiKey());

            //@ URL 세팅
            String url = fmpApi.url;
            url = ClientUtil.addQueryParams(url, reqData, true);

            AsyncRequestDto<R> requestData = AsyncRequestDto.<R>builder()
                    .id(idExtractor.apply(reqData))
                    .url(url)
                    .method(HttpMethod.GET)
                    .responseType(responseType)
                    .build();

            sendBodyList.add(requestData);
        }

        return httpClientComponent.exchangeParallel(sendBodyList);
    }


}
