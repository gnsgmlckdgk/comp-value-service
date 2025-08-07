package com.finance.dart.api.abroad.service;

import com.finance.dart.api.abroad.dto.financial.statement.AssetsCurrent;
import com.finance.dart.api.abroad.dto.financial.statement.OperatingIncomeLossDto;
import com.finance.dart.api.abroad.enums.SecApiList;
import com.finance.dart.api.abroad.util.SecUtil;
import com.finance.dart.common.service.ConfigService;
import com.finance.dart.common.service.HttpClientService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 기업 프로파일 서비스
 */
@Slf4j
@AllArgsConstructor
@Service
public class AbroadFinancialStatementService {

    private final ConfigService configService;
    private final HttpClientService httpClientService;


    /**
     * 영업이익 조회
     * @param cik
     * @return
     */
    public OperatingIncomeLossDto findFS_OperatingIncomeLoss(String cik) {

        OperatingIncomeLossDto operatingIncomeLossDto =
                findFinancialStatementDetail(cik, SecApiList.OperatingIncomeLoss, new ParameterizedTypeReference<>() {});

        return operatingIncomeLossDto;
    }

    /**
     * 유동자산 합계 조회
     * @param cik
     * @return
     */
    public AssetsCurrent findFS_AssetsCurrent(String cik) {

        AssetsCurrent assetsCurrent =
                findFinancialStatementDetail(cik, SecApiList.AssetsCurrent, new ParameterizedTypeReference<>() {});

        return assetsCurrent;
    }


    // private ---------------------------------------------------------------------

    /**
     * 재무제표 상세항목 조회
     * @param cik
     * @param detailType
     * @return
     */
    private <T> T findFinancialStatementDetail(String cik, SecApiList detailType, ParameterizedTypeReference<T> typeRef) {

        //@ 요청 데이터 세팅
        String url = detailType.url;
        url = SecUtil.setUrlCik(url, cik);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("User-Agent", "MyFinanceTool/1.0 (contact: dohauzi@gmail.com)");

        //@ 요청
        ResponseEntity<T> response =
                httpClientService.exchangeSync(url, HttpMethod.GET, headers, typeRef);

        //@ 응답데이터 가공
        T responseBody = response.getBody();

        return responseBody;
    }

}
