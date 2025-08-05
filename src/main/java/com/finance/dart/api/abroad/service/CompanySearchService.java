package com.finance.dart.api.abroad.service;

import com.finance.dart.api.abroad.dto.FindCompanySymbolReqDto;
import com.finance.dart.api.abroad.dto.FindCompanySymbolResDto;
import com.finance.dart.api.abroad.enums.ExchangeConst;
import com.finance.dart.api.abroad.enums.FmpApiList;
import com.finance.dart.common.service.ConfigService;
import com.finance.dart.common.service.HttpClientService;
import com.finance.dart.common.util.ClientUtil;
import com.finance.dart.common.util.ConvertUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class CompanySearchService {

    private final ConfigService configService;
    private final HttpClientService httpClientService;

    /**
     * 기업 심볼 목록 검색
     * @param companyName
     * @return
     */
    public List<FindCompanySymbolResDto> findSymbolListByCompanyName(String companyName) {

        //@ 요청 데이터 세팅
        String apiKey = configService.getFmpApiKey();
        String url = FmpApiList.CompanyNameSearch.url;

        FindCompanySymbolReqDto paramDto = FindCompanySymbolReqDto.ofQuery(apiKey, companyName);
        Map<String, Object> paramMap = ConvertUtil.toMap(paramDto, false);
        url = ClientUtil.addQueryParams(url, ConvertUtil.toStringMap(paramMap, false));

        //@ 요청
        ResponseEntity<List<FindCompanySymbolResDto>> response =
                httpClientService.exchangeSync(url, HttpMethod.GET, new ParameterizedTypeReference<>() {});

        //@ 응답데이터 가공
        List<FindCompanySymbolResDto> findCompanySymbolResDtoList = response.getBody();

        // 미국거래소만 분류
        return filterOnlyUsExchanges(findCompanySymbolResDtoList);
    }

    /**
     * 미국 거래소만 분류
     * @param list
     * @return
     */
    private List<FindCompanySymbolResDto> filterOnlyUsExchanges(List<FindCompanySymbolResDto> list) {
        return list.stream()
                .filter(findCompany -> Arrays.stream(ExchangeConst.US_EXCHANGES).anyMatch(
                        usExchange -> usExchange.equalsIgnoreCase(findCompany.getExchange())
                )).toList();
    }

}
