package com.finance.dart.api.abroad.service;

import com.finance.dart.api.abroad.dto.financial.statement.CommonFinancialStatementDto;
import com.finance.dart.api.abroad.dto.financial.statement.USD;
import com.finance.dart.api.abroad.dto.financial.statement.Units;
import com.finance.dart.api.abroad.enums.SecApiList;
import com.finance.dart.api.abroad.util.DebtCalculator;
import com.finance.dart.api.abroad.util.EquityCalculator;
import com.finance.dart.api.abroad.util.SecUtil;
import com.finance.dart.api.abroad.util.SharesOutstandingExtractor;
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
     * 기업 제무정보 전체 조회
     * @param cik
     * @return
     */
    public Map<String, Object> findFS_Companyfacts(String cik) {
        Map<String, Object> financialStatement =
                findFinancialStatementDetail(cik, SecApiList.Companyfacts, new ParameterizedTypeReference<>() {});
        return financialStatement;
    }

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

        /**
         * 값 검증도 다시 ( 애플 : "고정부채": "124545000000" )
         * LiabilitiesNoncurrent(고정부채) = Liabilities - LiabilitiesCurrent
         * 	•	us-gaap:Liabilities → 총부채 (Current + Non-current)
         * 	•	us-gaap:LiabilitiesCurrent → 유동부채
         *
         * 여기서 총부채값이 없는 경우도 있어서
         * 자산(Assets) = 부채(Liabilities) + 지분(Equity)
         * => 지분 : StockholdersEquityIncludingPortionAttributableToNoncontrollingInterest (전체지분)
         *          없으면 StockholdersEquity (지배배주주몫의 지분) + NoncontrollingInterest (비지배지분) : 이것도 둘중하나만 있을 수 있음 그럼 둘 중 하나만 사용
         *          셋 다 없는경우는 계산 실패 처리
         * 부채(Liabilities) = 자산(Assets) − 지분(Equity)
         * 고정부채(Noncurrent Liabilities) = 총부채(Liabilities) − 유동부채(Current Liabilities)
         * 로 계산
         */

        //@1. 고정부채 조회 (고정부채만 조회, 기업에 따라 데이터가 없는 경우도 있음)
        CommonFinancialStatementDto financialStatement =
                findFinancialStatementDetail(cik, SecApiList.LiabilitiesNoncurrent, new ParameterizedTypeReference<>() {});

        if(financialStatement != null) return financialStatement;

        // 유동부채
        CommonFinancialStatementDto liabilitiesCurrent =  findFS_LiabilitiesCurrent(cik);
        if(log.isDebugEnabled()) log.debug("[findFS_LiabilitiesNoncurrent] 유동부채 = {}", liabilitiesCurrent);

        //@2. 고정부채 = 총부채 - 유동부채
        CommonFinancialStatementDto liabilities = findFS_Liabilities(cik);  // 총부채
        if(log.isDebugEnabled()) log.debug("[findFS_LiabilitiesNoncurrent] 총부채 = {}", liabilities);
        if(liabilities != null) {   // 총부채 데이터가 없는 경우도 있음

            Units liabilitiesUnits = liabilities.getUnits();
            Units liabilitiesCurrentUnits = liabilitiesCurrent.getUnits();

            // 고정부채 계산
            List<USD> liabilitiesNoncurrentUsd =
                    DebtCalculator.calculateNonCurrentLiabilitiesRobust(liabilitiesUnits.getUsd(), liabilitiesCurrentUnits.getUsd());
            if(log.isDebugEnabled()) log.debug("[findFS_LiabilitiesNoncurrent] 고정부채 = 총부채-유동부채 계산값 : {}", liabilitiesNoncurrentUsd);

            // 고정부채로 값 변경
            Units noncurrentUnits = new Units();
            noncurrentUnits.setUsd(liabilitiesNoncurrentUsd);
            liabilities.setUnits(noncurrentUnits); // 총부채 -> 고정부채 데이터로 변경

            return liabilities;
        }

        //@3. 고정부채 = 부채파생(총부채) - 유동부채
        //@ 부채 = 자산 - 지분
        // 자산
        CommonFinancialStatementDto assets =
                findFinancialStatementDetail(cik, SecApiList.Assets, new ParameterizedTypeReference<>() {});
        if(log.isDebugEnabled()) log.debug("[findFS_LiabilitiesNoncurrent] 자산 = {}", assets);

        // 전체지분
        CommonFinancialStatementDto totalEquity =
                findFinancialStatementDetail(cik, SecApiList.StockholdersEquityIncludingPortionAttributableToNoncontrollingInterest, new ParameterizedTypeReference<>() {});
        if(log.isDebugEnabled()) log.debug("[findFS_LiabilitiesNoncurrent] 전체지분 = {}", totalEquity);

        //# 전체지분이 조회가 안되면 지분정보를 조회해서 전체지분을 파생
        if(totalEquity == null) {   // 전체지분 데이터가 없는 경우
            // 지배주주 지분
            CommonFinancialStatementDto holdersEquity =
                    findFinancialStatementDetail(cik, SecApiList.StockholdersEquity, new ParameterizedTypeReference<>() {});
            if(log.isDebugEnabled()) log.debug("[findFS_LiabilitiesNoncurrent] 지배주주 = {}", holdersEquity);

            // 비지배주주 지분
            CommonFinancialStatementDto nocontrollingInterest =
                    findFinancialStatementDetail(cik, SecApiList.NoncontrollingInterest, new ParameterizedTypeReference<>() {});
            if(log.isDebugEnabled()) log.debug("[findFS_LiabilitiesNoncurrent] 비지배주주 = {}", nocontrollingInterest);

            if(holdersEquity == null && nocontrollingInterest == null) {    // 계산 불가
                return null;
            } else if (holdersEquity == null) {
                totalEquity.setUnits(nocontrollingInterest.getUnits());
            } else if (nocontrollingInterest == null) {
                totalEquity.setUnits(holdersEquity.getUnits());
            } else {
                // 계산
                List<USD> holdersEquityUsd = holdersEquity.getUnits().getUsd();
                List<USD> nocontrollingInterestUsd = nocontrollingInterest.getUnits().getUsd();
                List<USD> totalUsd = EquityCalculator.calculateTotalEquityRobust(holdersEquityUsd, nocontrollingInterestUsd);
                if(log.isDebugEnabled()) log.debug("[findFS_LiabilitiesNoncurrent] 전체지분 = 지배주주지분+비지배주주지분 계산값 : {}", totalUsd);

                Units totlaUnits = totalEquity.getUnits();
                totlaUnits.setUsd(totalUsd);
                totalEquity.setUnits(totlaUnits);
            }
        }

        //# 자산과 전체지분을 통해서 전체부채를 파생
        List<USD> totalDebtUsd =
                DebtCalculator.calculateLiabilitiesFromAssetsAndEquityRobust(assets.getUnits().getUsd(), totalEquity.getUnits().getUsd());
        if(log.isDebugEnabled()) log.debug("[findFS_LiabilitiesNoncurrent] 전체부채 = 자산-전체지분 계산값 : {}", totalDebtUsd);

        //# 전체부채에서 유동부채를 빼서 고정부채를 파생
        List<USD> nonCurrentLibUsd =
                DebtCalculator.calculateNonCurrentLiabilitiesRobust(totalDebtUsd, liabilitiesCurrent.getUnits().getUsd());
        if(log.isDebugEnabled()) log.debug("[findFS_LiabilitiesNoncurrent] 고정부채 = 전체부채-유동부채 계산값 : {}", nonCurrentLibUsd);

        // 자산 조회한 데이터에 고정부채 데이터로 변경 후 return
        Units assetsUnits = assets.getUnits();
        assetsUnits.setUsd(nonCurrentLibUsd);
        assets.setUnits(assetsUnits);
        if(log.isDebugEnabled()) log.debug("[findFS_LiabilitiesNoncurrent] 고정부채 = {}", assets);

        return assets;
    }

    /**
     * 발행주식수 조회
     * @param cik
     * @return
     */
    public String getStockSharesOutstanding(String cik) {

        /** 2025.09.11 : 발행주식수 정보가 특정 태그에 확정적으로 고정이 안되어있어서
         *  회사 전체 facts에서 최신 outstanding을 우선 탐색하고,
         *  실패 시 기존 단일 태그(EntityCommonStockSharesOutstanding) 방식으로 폴백한다.
         **/
        String result = "";

        // 값이 int 인지 최대값으로 조회되는경우가 있음 그래서 그냥 사용 안함
//        //@ dei:EntityCommonStockSharesOutstanding 먼저 조회 후 없으면 전체 제무제표 정보에서 검색
//        CommonFinancialStatementDto financialStatement =
//                findFinancialStatementDetail(cik, SecApiList.EntityCommonStockSharesOutstanding, new ParameterizedTypeReference<>() {});
//
//        if(financialStatement != null && financialStatement.getUnits() != null) {
//            List<Shares> sharesList = SecUtil.getSharesList(financialStatement);
//            Shares shares = SecUtil.getSharesByOffset(sharesList, 0);   // 가장 최근 데이터
//
//            long sharesVal = Math.round(shares.getVal());
//            result = String.valueOf(sharesVal);
//
//            return result;
//        }

        //@ 전체 제무정보 조회
        Map<String, Object> companyfacts = findFS_Companyfacts(cik);

        //@ 전체 facts에서 최신 발행주식수 탐색 (차원합산 허용, 보통주 우선)
        SharesOutstandingExtractor.SharesResult r =
                SharesOutstandingExtractor.extractLatestShares(companyfacts, true, true);

        if (r != null) {
            // shares는 정수 취급: double -> long으로 반올림 변환
            long sharesVal = Math.round(r.value);
            result = String.valueOf(sharesVal);
        }

        return result;
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
