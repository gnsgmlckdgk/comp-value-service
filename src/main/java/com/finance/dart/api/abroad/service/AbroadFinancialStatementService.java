package com.finance.dart.api.abroad.service;

import com.finance.dart.api.abroad.dto.financial.statement.CommonFinancialStatementDto;
import com.finance.dart.api.abroad.dto.financial.statement.USD;
import com.finance.dart.api.abroad.dto.financial.statement.Units;
import com.finance.dart.api.abroad.enums.SecApiList;
import com.finance.dart.api.abroad.util.DebtCalculator;
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
import java.util.List;
import java.util.Map;

/**
 * 기업 재무제표 조회 서비스
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
    public CommonFinancialStatementDto findFS_OperatingIncomeLoss(String cik) {
        CommonFinancialStatementDto financialStatement =
                findFinancialStatementDetail(cik, SecApiList.OperatingIncomeLoss, new ParameterizedTypeReference<>() {});
        return financialStatement;
    }

    /**
     * 유동자산 합계 조회
     * @param cik
     * @return
     */
    public CommonFinancialStatementDto findFS_AssetsCurrent(String cik) {
        CommonFinancialStatementDto financialStatement =
                findFinancialStatementDetail(cik, SecApiList.AssetsCurrent, new ParameterizedTypeReference<>() {});
        return financialStatement;
    }

    /**
     * 유동부채 합계 조회
     * @param cik
     * @return
     */
    public CommonFinancialStatementDto findFS_LiabilitiesCurrent(String cik) {
        CommonFinancialStatementDto financialStatement =
                findFinancialStatementDetail(cik, SecApiList.LiabilitiesCurrent, new ParameterizedTypeReference<>() {});
        return financialStatement;
    }

    /**
     * <pre>
     * 비유동자산내 투자자산 조회
     * 1. 장기매도 가능 증권
     * </pre>
     * @param cik
     * @return
     */
    public CommonFinancialStatementDto findFS_NI_AvailableForSaleSecuritiesNoncurrent(String cik) {
        CommonFinancialStatementDto financialStatement =
                findFinancialStatementDetail(cik, SecApiList.AvailableForSaleSecuritiesNoncurrent, new ParameterizedTypeReference<>() {});
        return financialStatement;
    }

    /**
     * <pre>
     * 비유동자산내 투자자산 조회
     * 2. 지분법 투자
     * </pre>
     * @param cik
     * @return
     */
    public CommonFinancialStatementDto findFS_NI_LongTermInvestments(String cik) {
        CommonFinancialStatementDto financialStatement =
                findFinancialStatementDetail(cik, SecApiList.LongTermInvestments, new ParameterizedTypeReference<>() {});
        return financialStatement;
    }

    /**
     * <pre>
     * 비유동자산내 투자자산 조회
     * 3. 기타 장기투자
     * </pre>
     * @param cik
     * @return
     */
    public CommonFinancialStatementDto findFS_NI_OtherInvestments(String cik) {
        CommonFinancialStatementDto financialStatement =
                findFinancialStatementDetail(cik, SecApiList.OtherInvestments, new ParameterizedTypeReference<>() {});
        return financialStatement;
    }

    /**
     * <pre>
     * 비유동자산내 투자자산 조회
     * 4. 투자 및 대여금
     * </pre>
     * @param cik
     * @return
     */
    public CommonFinancialStatementDto findFS_NI_InvestmentsAndAdvances(String cik) {
        CommonFinancialStatementDto financialStatement =
                findFinancialStatementDetail(cik, SecApiList.InvestmentsAndAdvances, new ParameterizedTypeReference<>() {});
        return financialStatement;
    }

    /**
     * 총 부채 조회
     * @param cik
     * @return
     */
    public CommonFinancialStatementDto findFS_Liabilities(String cik) {

        CommonFinancialStatementDto financialStatement =
                findFinancialStatementDetail(cik, SecApiList.Liabilities, new ParameterizedTypeReference<>() {});

        return financialStatement;
    }

    /**
     * 고정부채 합계 조회
     * @param cik
     * @return
     */
    public CommonFinancialStatementDto findFS_LiabilitiesNoncurrent(String cik) {

        // 고정부채 조회 (고정부채만 조회, 기업에 따라 데이터가 없는 경우도 있음)
        CommonFinancialStatementDto financialStatement =
                findFinancialStatementDetail(cik, SecApiList.LiabilitiesNoncurrent, new ParameterizedTypeReference<>() {});

        if(financialStatement == null) {
            /**
             * 값 검증도 다시 ( 애플 : "고정부채": "124545000000" )
             * LiabilitiesNoncurrent(고정부채) = Liabilities - LiabilitiesCurrent
             * 	•	us-gaap:Liabilities → 총부채 (Current + Non-current)
             * 	•	us-gaap:LiabilitiesCurrent → 유동부채
             */

            CommonFinancialStatementDto liabilities = findFS_Liabilities(cik);  // 총부채
            CommonFinancialStatementDto liabilitiesCurrent =  findFS_LiabilitiesCurrent(cik);   // 유동부채

            Units liabilitiesUnits = liabilities.getUnits();
            Units liabilitiesCurrentUnits = liabilitiesCurrent.getUnits();

            //@ 고정부채 계산
            List<USD> liabilitiesNoncurrentUsd =
                    DebtCalculator.calculateNonCurrentLiabilitiesRobust(liabilitiesUnits.getUsd(), liabilitiesCurrentUnits.getUsd());

            //@ 고정부채로 값 변경
            Units noncurrentUnits = new Units();
            noncurrentUnits.setUsd(liabilitiesNoncurrentUsd);
            liabilities.setUnits(noncurrentUnits); // 총부채 -> 고정부채 데이터로 변경

            return liabilities;
        }

        return financialStatement;
    }

    /**
     * 발행주식수 조회
     * @param cik
     * @return
     */
    public CommonFinancialStatementDto findFS_EntityCommonStockSharesOutstanding(String cik) {
        CommonFinancialStatementDto financialStatement =
                findFinancialStatementDetail(cik, SecApiList.EntityCommonStockSharesOutstanding, new ParameterizedTypeReference<>() {});
        return financialStatement;
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

        try {
            //@ 요청
            ResponseEntity<T> response =
                    httpClientService.exchangeSync(url, HttpMethod.GET, headers, null, typeRef);

            //@ 응답데이터 가공
            T responseBody = response.getBody();

            return responseBody;

        } catch (Exception e) {
            // 재무제표에 없는 항목은 XML 로 NoSuchKey 응답이와서 null return 처리
            return null;
        }

    }

}
