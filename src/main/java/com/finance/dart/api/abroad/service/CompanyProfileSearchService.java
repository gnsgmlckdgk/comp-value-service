package com.finance.dart.api.abroad.service;

import com.finance.dart.api.abroad.dto.company.CompanyProfileDataReqDto;
import com.finance.dart.api.abroad.dto.company.CompanyProfileDataResDto;
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

import java.util.List;

/**
 * 기업 프로파일 서비스
 */
@Slf4j
@AllArgsConstructor
@Service
public class CompanyProfileSearchService {

    private final ConfigService configService;
    private final HttpClientService httpClientService;


    /**
     * 기업 프로파일 목록 조회
     * @param symbol
     * @return
     */
    public List<CompanyProfileDataResDto> findProfileListBySymbol(String symbol) {

        //@ 요청 데이터 세팅
        String apiKey = configService.getFmpApiKey();
        String url = FmpApiList.CompanyProfileData.url;

        CompanyProfileDataReqDto paramDto = CompanyProfileDataReqDto.of(apiKey, symbol);
        url = ClientUtil.addQueryParams(url, paramDto, true);

        //@ 요청
        ResponseEntity<List<CompanyProfileDataResDto>> response =
                httpClientService.exchangeSync(url, HttpMethod.GET, new ParameterizedTypeReference<>() {});

        //@ 응답데이터 가공
        List<CompanyProfileDataResDto> findCompanySymbolResDtoList = response.getBody();

        return findCompanySymbolResDtoList;
    }

}
