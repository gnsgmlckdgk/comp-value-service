package com.finance.dart.api.service;

import com.finance.dart.api.dto.*;
import com.finance.dart.api.enums.FsDiv;
import com.finance.dart.api.enums.ReprtCode;
import com.finance.dart.common.util.CalUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class CalPerValueService {
    final String EXEC_ACCT_ID = "-표준계정코드 미사용-"; // 제외 항목

    private final CorpCodeService corpCodeService;
    private final FinancialStatmentService financialStatmentService;
    private final NumberOfSharesIssuedService numberOfSharesIssuedService;

    public Object calPerValue(String year) {

        //@1. 상장 기업목록 조회
        List<CorpCodeDTO> corpList = getCorpCompanyCodeList();

        //@2. 각 기업 1주당 가치 계산 및 스크리닝
        List<CorpCodeDTO> screeningCorp = companyOneStockValue(corpList, year);
        return screeningCorp;

        //@3. 결과 엑셀출력 및 응답(개발 중단)
    }




    /**
     * 상장 기업목록 조회
     * @return
     */
    private List<CorpCodeDTO> getCorpCompanyCodeList() {
        CorpCodeResDTO corpCodeResDTO = corpCodeService.getCorpCode(true);
        return corpCodeResDTO.getList();
    }

    /**
     * 기업 1주당 가치 계산
     * @param stockCompanyList
     * @param year
     * @return
     */
    private List<CorpCodeDTO> companyOneStockValue(List<CorpCodeDTO> stockCompanyList, String year) {

        List<CorpCodeDTO> resultCompanyList = new LinkedList<>();

        //@ 계산 및 스크리닝
        int i = 0;  // 조회 개수 체크
        for(CorpCodeDTO corpCodeDTO : stockCompanyList) {
            String oneStockValue = underValueCheck(corpCodeDTO, year);
            corpCodeDTO.setOneStockValue(oneStockValue);
            resultCompanyList.add(corpCodeDTO);
            i++;

            if(i == 3) break;  // 조회 개수 제한
        }

        return resultCompanyList;
    }

    /**
     * 기업 가치 평가
     * @param corpCodeDTO 상장기업정보
     * @param year 기준연도
     * @return true: 저평가
     */
    private String underValueCheck(CorpCodeDTO corpCodeDTO, String year) {

        String result = "NULL";   // 결과

        int tYear = Integer.parseInt(year); // 기준연도

        //@ 재무정보 조회(전전기, 전기, 당기)
        FsDiv selFsDiv = FsDiv.연결;  // 기본값: 연결 / 연결없는경우 개별

        // 전전기
        FinancialStatementResDTO financialStatementResDTO =
                getFinancialStatement(corpCodeDTO, String.valueOf(tYear-3), ReprtCode.사업보고서, selFsDiv);
        if(null == financialStatementResDTO.getList()) {
            selFsDiv = FsDiv.개별;
            financialStatementResDTO = getFinancialStatement(corpCodeDTO, String.valueOf(tYear-3), ReprtCode.사업보고서, selFsDiv);
            if(null == financialStatementResDTO.getList()) {
                // 개별도 없으면 해당기업 계산 생략
                return result;
            }
        }
        Map<String, FinancialStatementDTO> fss03 = cleanUpFinancailStatement(financialStatementResDTO);

        // 전기
        Map<String, FinancialStatementDTO> fss02 =
                cleanUpFinancailStatement(
                        getFinancialStatement(corpCodeDTO, String.valueOf(tYear-2), ReprtCode.사업보고서, selFsDiv));

        // 당기
        String selFinanceBungiCode = ReprtCode.사업보고서.getCode();
        FinancialStatementResDTO fss01Res =
                getFinancialStatement(corpCodeDTO, String.valueOf(tYear-1), ReprtCode.사업보고서, selFsDiv);

        if(null == fss01Res.getList()) {
            // 사업보고서가 없는 경우(4분기 발표전 다음해) 작년3분기 조회
            fss01Res = getFinancialStatement(corpCodeDTO, String.valueOf(tYear-1), ReprtCode.분기보고서_3, selFsDiv);
            selFinanceBungiCode = ReprtCode.분기보고서_3.getCode();

        } else if(null == fss01Res.getList() && selFinanceBungiCode.equals(ReprtCode.분기보고서_3.getCode())) {
            // 3분기보고서도 없으면 2분기보고서(반기보고서)
            fss01Res = getFinancialStatement(corpCodeDTO, String.valueOf(tYear-1), ReprtCode.반기보고서, selFsDiv);
            selFinanceBungiCode = ReprtCode.반기보고서.getCode();
        }

        Map<String, FinancialStatementDTO> fss01 = cleanUpFinancailStatement(fss01Res);

        //@ 저평가 확인
        //# 단위
        final int UNIT = 100000000; // 기본 1억

        //# 사업가치 [영업이익(전전기/전기/당기)]
        String bussValue = calBussValue(fss03, fss02, fss01, selFinanceBungiCode);

        //# 재산가치 [유동자산, 유동부채, 유동비율, 투자자산(비유동자산내)]
        String properValue = calProperValue(fss01);

        //# 고정부채(비유동부채)
        String fixedLb = getFixedLiabilities(fss01);

        //# 발행주식수
        String selNoIBungiCode = ReprtCode.사업보고서.getCode();
        NumberOfSharesIssuedResDTO numberOfSharesIssuedResDTO =
                getNumberOfSharesIssued(corpCodeDTO, String.valueOf(tYear-1), ReprtCode.사업보고서);

        if(null == numberOfSharesIssuedResDTO.getList()) {
            // 사업보고서가 없는 경우(4분기 발표전 다음해) 작년3분기 조회
            selNoIBungiCode = ReprtCode.분기보고서_3.getCode();
            numberOfSharesIssuedResDTO =
                    getNumberOfSharesIssued(corpCodeDTO, String.valueOf(tYear-1), ReprtCode.분기보고서_3);

        }else if(null == numberOfSharesIssuedResDTO.getList() && selNoIBungiCode.equals(ReprtCode.분기보고서_3.getCode())) {
            // 3분기보고서도 없으면 2분기보고서(반기보고서)
            selNoIBungiCode = ReprtCode.반기보고서.getCode();
            numberOfSharesIssuedResDTO =
                    getNumberOfSharesIssued(corpCodeDTO, String.valueOf(tYear-1), ReprtCode.반기보고서);
        }
        String totalStockShareIssue = getCommonStockTotalShareIssue(numberOfSharesIssuedResDTO);    // 발행주식수

        //# 결과
        // 주주 가치 (사업가치 + 재산가치 - 부채)
        String jujuValue = CalUtil.add(bussValue, properValue);
        jujuValue = CalUtil.sub(jujuValue, fixedLb);

        // 1주당가치 (주주가치 / 발행주식수)
        result = CalUtil.divide(jujuValue, totalStockShareIssue, RoundingMode.HALF_UP);

        return result;
    }

    /**
     * 보통주 : 총 주식 발행 수
     * @param numberOfSharesIssuedResDTO
     * @return
     */
    private String getCommonStockTotalShareIssue(NumberOfSharesIssuedResDTO numberOfSharesIssuedResDTO) {

        List<NumberOfSharesIssuedDTO> numberOfSharesIssuedDTOList = numberOfSharesIssuedResDTO.getList();

        for(NumberOfSharesIssuedDTO numberOfSharesIssuedDTO : numberOfSharesIssuedDTOList) {
            if("보통주".equals(numberOfSharesIssuedDTO.getSe())) {
                return numberOfSharesIssuedDTO.getIstc_totqy();
            }
        }

        return "";
    }

    /**
     * 주식의 총 수 현황 조회
     * @param corpCodeDTO
     * @param year
     * @param reprtCode
     * @return
     */
    private NumberOfSharesIssuedResDTO getNumberOfSharesIssued(CorpCodeDTO corpCodeDTO, String year, ReprtCode reprtCode) {

        NumberOfSharesIssuedResDTO numberOfSharesIssuedResDTO =
                numberOfSharesIssuedService.getNumberOfSharesIssued(corpCodeDTO.getCorpCode(), year, reprtCode.getCode());

        return numberOfSharesIssuedResDTO;
    }


    /**
     * 고정부채(비유동부채)
     * @param fss01
     * @return
     */
    private String getFixedLiabilities(Map<String, FinancialStatementDTO> fss01) {
        final String FiXED_LB_KEY = "ifrs_NoncurrentLiabilities";
        FinancialStatementDTO fs = fss01.get(FiXED_LB_KEY);
        return fs.getThstrmAmount();
    }

    /**
     * 재산가치 계산
     * @param fss01
     * @return
     */
    private String calProperValue(
            Map<String, FinancialStatementDTO> fss01) {


        // 유동자산 : ifrs_CurrentAssets
        final String CA_KEY = "ifrs_CurrentLiabilities";
        FinancialStatementDTO fs01_ca = fss01.get(CA_KEY);
        String caValue = fs01_ca.getThstrmAmount(); // 유동자산

        // 유동부채 : ifrs_CurrentLiabilities
        final String CL_KEY = "ifrs_CurrentLiabilities";
        FinancialStatementDTO fs01_cl = fss01.get(CL_KEY);
        String clValue = fs01_cl.getThstrmAmount(); // 유동부채

        // 유동비율 : (유동자산 / 유동부채)
        String crValue = CalUtil.divide(caValue, clValue, 2, RoundingMode.HALF_UP);

        /**
         * 투자자산(비유동자산내)
         *
         * 종속/관계/공동기업 투자 관련
         * ifrs_InvestmentsInSubsidiariesJointVenturesAndAssociates
         * ifrs_InvestmentInAssociate
         * ifrs_InvestmentsInJointVentures
         * ifrs_InvestmentsInSubsidiaries
         *
         * 금융자산 관련
         * ifrs_NoncurrentFinancialAssets
         * ifrs_OtherNoncurrentFinancialAssets
         * ifrs_FinancialAssetsAtFairValueThroughProfitOrLoss
         * ifrs_FinancialAssetsAtFairValueThroughOtherComprehensiveIncome
         * ifrs_AvailableForSaleFinancialAssets
         * ifrs_HeldToMaturityInvestments
         *
         * 투자부동산
         * ifrs_InvestmentProperty
         *
         * 기타 투자
         * ifrs_OtherInvestments
         * ifrs_LongTermInvestments
         * ifrs_EquityInvestments
         */
        final String IA_KEY_ARR[] = {
                  "ifrs_InvestmentsInSubsidiariesJointVenturesAndAssociates"
                , "ifrs_InvestmentInAssociate"
                , "ifrs_InvestmentsInJointVentures"
                , "ifrs_InvestmentsInSubsidiaries"
                , "ifrs_NoncurrentFinancialAssets"
                , "ifrs_OtherNoncurrentFinancialAssets"
                , "ifrs_FinancialAssetsAtFairValueThroughProfitOrLoss"
                , "ifrs_FinancialAssetsAtFairValueThroughOtherComprehensiveIncome"
                , "ifrs_AvailableForSaleFinancialAssets"
                , "ifrs_HeldToMaturityInvestments"
                , "ifrs_InvestmentProperty"
                , "ifrs_OtherInvestments"
                , "ifrs_LongTermInvestments"
                , "ifrs_EquityInvestments"
        };

        String iaValueSum = "0";    // 투자자산 합계
        for(final String IA_KEY : IA_KEY_ARR) {
            FinancialStatementDTO fs01_ia = fss01.get(IA_KEY);
            String iaValue = fs01_ia.getThstrmAmount();
            iaValueSum = CalUtil.add(iaValueSum, iaValue);
        }

        // 유동자산 - (유동부채 * 1.2) + 투자자산
        String value01 = CalUtil.multi(clValue, crValue);   // 유동부채 * 유동비율
        String value02 = CalUtil.sub(caValue, value01);     // 유동자산 - (value01)
        String result = CalUtil.add(value02, iaValueSum);   // value02 + 투자자산

        return result;
    }

    /**
     * 사업가치 계산
     * @param fss03
     * @param fss02
     * @param fss01
     * @param selBoongiCode
     * @return
     */
    private String calBussValue(
            Map<String, FinancialStatementDTO> fss03,
            Map<String, FinancialStatementDTO> fss02,
            Map<String, FinancialStatementDTO> fss01,
            String selBoongiCode) {

        final String BUSS_KEY = "dart_OperatingIncomeLoss"; // 영업이익

        FinancialStatementDTO fs03 = fss03.get(BUSS_KEY);
        FinancialStatementDTO fs02 = fss02.get(BUSS_KEY);
        FinancialStatementDTO fs01 = fss01.get(BUSS_KEY);

        // thstrm_amount (당기 금액)
        String bussPf03 = fs03.getThstrmAmount();   // 전전기
        String bussPf02 = fs02.getThstrmAmount();   // 전기

        String bussPf01 = fs01.getThstrmAmount();   // 당기
        if(selBoongiCode.equals(ReprtCode.분기보고서_3.getCode())) {
            // 3분기 영업이익에 누적영업이익 더하기
            bussPf01 = CalUtil.add(bussPf01, fs01.getThstrmAddAmount());

        } else if(selBoongiCode.equals(ReprtCode.반기보고서.getCode())) {
            // 2분기 영업이익에 누적영업이익 더하고 2분기 영업이익 한번 더 더하기
            bussPf01 = CalUtil.add(bussPf01, fs01.getThstrmAddAmount());
            bussPf01 = CalUtil.add(bussPf01, fs01.getThstrmAmount());
        }

        String sum = CalUtil.add(CalUtil.add(bussPf03, bussPf02), bussPf01);
        String average = CalUtil.divide(sum, "3", 2, RoundingMode.HALF_UP);

        return CalUtil.multi(average, "10");
    }

    /**
     * 재무정보 조회(당기:사업보고서)
     * @param corpCodeDTO
     * @param year
     * @param reprtCode
     * @param fsDiv
     * @return
     */
    private FinancialStatementResDTO getFinancialStatement(CorpCodeDTO corpCodeDTO, String year, ReprtCode reprtCode, FsDiv fsDiv) {

        FinancialStatementResDTO financialStatementResDTO = financialStatmentService.getCompanyFinancialStatement(
                corpCodeDTO.getCorpCode(), String.valueOf(year), reprtCode.getCode(), fsDiv.getCode());

        return financialStatementResDTO;
    }

    /**
     * 계정ID로 재무 데이터 정리
     * @param financialStatementResDTO
     * @return
     */
    private Map<String, FinancialStatementDTO> cleanUpFinancailStatement(
            FinancialStatementResDTO financialStatementResDTO) {

        // 재무정보 List -> Map
        List<FinancialStatementDTO> fss = financialStatementResDTO.getList();   // 원본
        Map<String, FinancialStatementDTO> fs = new LinkedHashMap<>();          // 계정ID로 정리
        for(FinancialStatementDTO financialStatementDTO : fss) {

            String accoutId = financialStatementDTO.getAccountId();
            if(EXEC_ACCT_ID.equals(accoutId)) continue;

            fs.put(accoutId, financialStatementDTO);
        }

        return fs;
    }
}
