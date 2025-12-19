package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.dto.fmp.company.CompanyProfileDataReqDto;
import com.finance.dart.api.abroad.dto.fmp.company.CompanyProfileDataResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import com.finance.dart.common.component.ConfigComponent;
import com.finance.dart.common.component.HttpClientComponent;
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

    private final ConfigComponent configComponent;
    private final HttpClientComponent httpClientComponent;


    /**
     * <pre>
     * 기업 프로파일 목록 조회
     * 2025.08.12 : 배열로 받지만 한개의 정보만 조회되는게 정상
     * </pre>
     * @param symbol
     * @return
     */
    public List<CompanyProfileDataResDto> findProfileListBySymbol(String symbol) {

        //@ 요청 데이터 세팅
        String apiKey = configComponent.getFmpApiKey();
        String url = FmpApiList.CompanyProfileData.url;

        CompanyProfileDataReqDto paramDto = CompanyProfileDataReqDto.of(apiKey, symbol);
        url = ClientUtil.addQueryParams(url, paramDto, true);

        //@ 요청
        ResponseEntity<List<CompanyProfileDataResDto>> response =
                httpClientComponent.exchangeSync(url, HttpMethod.GET, new ParameterizedTypeReference<>() {});

        //@ 응답데이터 가공
        List<CompanyProfileDataResDto> findCompanySymbolResDtoList = response.getBody();

        return findCompanySymbolResDtoList;
    }

}
