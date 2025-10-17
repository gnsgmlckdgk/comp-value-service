package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.consts.ExchangeConst;
import com.finance.dart.api.abroad.dto.fmp.company.FindCompanySymbolReqDto;
import com.finance.dart.api.abroad.dto.fmp.company.FindCompanySymbolResDto;
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

import java.util.*;
import java.util.stream.Stream;

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
     * @param symbol
     * @return
     */
    public List<FindCompanySymbolResDto> findSymbolListByIntegrate(String companyName, String symbol) {

        //@ companyName
        List<FindCompanySymbolResDto> resultList01 = findSymbolListByCompanyName(companyName);

        //@ symbol
        List<FindCompanySymbolResDto> resultList02 = findSymbolListBySymbol(symbol);

        Map<String, FindCompanySymbolResDto> mergedMap = new LinkedHashMap<>();

        Stream.concat(resultList01.stream(), resultList02.stream())
                .forEach(s -> mergedMap.putIfAbsent(s.getSymbol(), s));

        List<FindCompanySymbolResDto> resultList = new LinkedList<>(mergedMap.values());

        // symbol과 동일하면 앞으로 정렬
        resultList.sort((a, b) -> {
            if (a.getSymbol().equals(symbol.toUpperCase())) return -1; // a가 기준 symbol이면 맨 앞
            if (b.getSymbol().equals(symbol.toUpperCase())) return 1;  // b가 기준 symbol이면 뒤로
            return 0; // 그 외는 순서 유지
        });

        return resultList;
    }

    /**
     * 기업 심볼 목록 검색(심볼로 검색)
     * @param symbol
     * @return
     */
    public List<FindCompanySymbolResDto> findSymbolListBySymbol(String symbol) {

        //@ 요청 데이터 세팅
        String apiKey = configService.getFmpApiKey();
        String url = FmpApiList.CompanyStockSymbolSearchBySymbol.url;

        FindCompanySymbolReqDto paramDto = FindCompanySymbolReqDto.ofQuery(apiKey, symbol);
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
     * 기업 심볼 목록 검색(기업명으로 검색)
     * @param companyName
     * @return
     */
    public List<FindCompanySymbolResDto> findSymbolListByCompanyName(String companyName) {

        //@ 요청 데이터 세팅
        String apiKey = configService.getFmpApiKey();
        String url = FmpApiList.CompanyStockSymbolSearchByCompanyName.url;

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
