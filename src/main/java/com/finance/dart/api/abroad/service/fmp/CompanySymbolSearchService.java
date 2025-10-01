package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.dto.fmp.company.FindCompanySymbolReqDto;
import com.finance.dart.api.abroad.dto.fmp.company.FindCompanySymbolResDto;
import com.finance.dart.api.abroad.consts.ExchangeConst;
import com.finance.dart.api.abroad.enums.FmpApiList;
import com.finance.dart.common.service.ConfigService;
import com.finance.dart.common.service.HttpClientService;
import com.finance.dart.common.util.ClientUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 기업 심볼검색 서비스
 */
@Slf4j
@AllArgsConstructor
@Service
public class CompanySymbolSearchService {

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
        String url = FmpApiList.CompanyStockSymbolSearch.url;

        FindCompanySymbolReqDto paramDto = FindCompanySymbolReqDto.ofQuery(apiKey, companyName);
        url = ClientUtil.addQueryParams(url, paramDto, true);

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
