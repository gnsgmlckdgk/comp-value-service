package com.finance.dart.api.abroad.component;

import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import com.finance.dart.api.abroad.enums.FmpApiList;
import com.finance.dart.common.service.ConfigService;
import com.finance.dart.common.service.HttpClientService;
import com.finance.dart.common.util.ClientUtil;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class FmpClientComponent {

    private final ConfigService configService;
    private final HttpClientService httpClientService;

    /**
     * FMP API GET 요청 전송
     * @param requestData
     * @return
     */
    public <T extends FmpReqCommon, R> R sendGet(FmpApiList fmpApi, T requestData, ParameterizedTypeReference<R> responseType) {

        //@ 요청 데이터
        requestData.setApikey(configService.getFmpApiKey());

        String url = fmpApi.url;
        url = ClientUtil.addQueryParams(url, requestData, true);

        //@ 요청
        ResponseEntity<R> response =
                httpClientService.exchangeSync(url, HttpMethod.GET, responseType);

        return response.getBody();
    }


}
