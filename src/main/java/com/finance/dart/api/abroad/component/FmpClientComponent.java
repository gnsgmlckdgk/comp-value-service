package com.finance.dart.api.abroad.component;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import com.finance.dart.api.abroad.enums.FmpApiList;
import com.finance.dart.common.component.ConfigComponent;
import com.finance.dart.common.component.HttpClientComponent;
import com.finance.dart.common.util.ClientUtil;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class FmpClientComponent {

    private final ConfigComponent configComponent;
    private final HttpClientComponent httpClientComponent;

    /**
     * FMP API GET 요청 전송
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


}
