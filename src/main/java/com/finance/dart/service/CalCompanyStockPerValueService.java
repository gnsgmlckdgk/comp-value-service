package com.finance.dart.service;

import com.finance.dart.common.enums.FsDiv;
import com.finance.dart.common.enums.ReprtCode;
import com.finance.dart.common.service.HttpClientService;
import com.finance.dart.common.util.CalUtil;
import com.finance.dart.common.util.ClientUtil;
import com.finance.dart.common.util.DateUtil;
import com.finance.dart.dto.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class CalCompanyStockPerValueService {

    private final String EXEC_ACCT_ID = "-표준계정코드 미사용-"; // 제외 항목

//    private FsDiv selFsDiv = FsDiv.연결;  // 기본값: 연결 / 연결없는경우 개별
//    private String selFinanceBungiCode = ReprtCode.사업보고서.getCode();
//    private StockValueResultDetailDTO resultDetail = new StockValueResultDetailDTO();   // 결과상세정보

    private final CorpCodeService corpCodeService;
    private final FinancialStatmentService financialStatmentService;
    private final NumberOfSharesIssuedService numberOfSharesIssuedService;

    private final HttpClientService httpClientService;


    @Getter
    private static class CalculationContext {
        private FsDiv selFsDiv;
        private String selFinanceBungiCode;
        private final StockValueResultDetailDTO resultDetail;

        private CalculationContext() {
            this.selFsDiv = FsDiv.연결;  // 기본값
            this.selFinanceBungiCode = ReprtCode.사업보고서.getCode();
            this.resultDetail = new StockValueResultDetailDTO();
        }
    }

    /**
     * 기업 한주당가치 계산
     * @param year
     * @param corpCode
     * @param corpName
     * @return
     */
    public StockValueResultDTO calPerValue(String year, String corpCode, String corpName) {

        CalculationContext context = new CalculationContext();

        StockValueResultDTO result = new StockValueResultDTO("정상 처리되었습니다.");

        //@1. 회사정보
        CorpCodeDTO corpCodeDTO = null;
        if(corpCode.equals("") && !corpName.equals("")) {   // 기업명으로 검색
            corpCodeDTO = corpCodeService.getCorpCodeFindName(true, corpName);
            if(corpCodeDTO == null) return new StockValueResultDTO("회사정보가 존재하지 않습니다.");
            corpCode = corpCodeDTO.getCorpCode();
        } else {
            corpCodeDTO = getCorpCode(corpCode);
        }

        result.set기업코드(corpCode);
        result.set기업명(corpCodeDTO.getCorpName());
        result.set주식코드(corpCodeDTO.getStockCode());

        //@2. 재무제표 조회(전전기/전기/당기)
        //# 전전기
        Map<String, FinancialStatementDTO> fss03 = getTwoYearsPriorFinancialStatements(year, corpCode, context);
        if(fss03 == null) {
            result.set주당가치("");
            result.set결과메시지("전전기 재무제표 정보가 존재하지 않습니다.");
            return result;
        }
        //# 전기
        Map<String, FinancialStatementDTO> fss02 = getPriorYearFinancialStatements(year, corpCode, context);
        //# 당기
        Map<String, FinancialStatementDTO> fss01 = getCurrentYearFinancialStatements(year, corpCode, context);

        //@3. 한주당 가치 계산
        String companyOneStockValue = calCompanyValue(corpCode, year, fss01, fss02, fss03, context);
        result.set주당가치(companyOneStockValue);

        result.set상세정보(context.getResultDetail());

        //@4. 현재가격 조회
        String currentStockValue = getCurrentValue(corpCodeDTO.getStockCode(), "KS");
        result.set현재가격(currentStockValue);

        //@5. 확인시간 추가
        result.set확인시간(DateUtil.getToday());

        return result;
    }

    /**
     * 현재 한주 가격 조회
     * @param stockCode     종목코드
     * @param exchangeCd    거래소코드(KS, KQ)
     * @return
     */
    private String getCurrentValue(String stockCode, String exchangeCd) {

        exchangeCd = "." + exchangeCd;

        // 종목코드 뒤에 .KS(코스피) 또는 .KQ(코스닥)를 붙여야 함 (ex: 005930.KS)
        String symbol = stockCode + exchangeCd;
        String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol;

        ResponseEntity<StockPriceDTO> response = httpClientService.exchangeSync(url,
                HttpMethod.GET, ClientUtil.createHttpEntity(MediaType.APPLICATION_JSON), StockPriceDTO.class);

        StockPriceDTO stockPriceDTO = response.getBody();
        StockPriceDTO.Meta meta = null;
        try {
            meta = stockPriceDTO.getChart().getResult().get(0).getMeta();
            if("".equals(validationResponse(meta))) {   // 정상
                return String.valueOf(Math.round(meta.getRegularMarketPrice()));   // 현재가
            }
            else if("1".equals(validationResponse(meta))) {
                // 코스피 종목을 코스닥에서 조회하거나 하면 검증1에서 걸릴 수 있어서 다시 조회
                if(".KS".equals(exchangeCd)) {
                    return getCurrentValue(stockCode, "KQ");   // 코스닥 조회
                } else if(".KQ".equals(exchangeCd)) {
                    return getCurrentValue(stockCode, "KS");   // 코스피 조회
                }
            } else {
                return "";
            }
        } catch (Exception e) {
            return "";
        }

        return "";
    }

    /**
     * 현재가격 응답정보 검증
     * @param meta
     * @return "" 정상
     */
    private String validationResponse(StockPriceDTO.Meta meta) {

        // 검증
        if (!"EQUITY".equals(meta.getInstrumentType())) {
            return "1";
        }

        // 가격이 null이거나 0 이하인지 확인
        if (meta.getRegularMarketPrice() == null || meta.getRegularMarketPrice() <= 0) {
            return "2";
        }

        // 거래소 코드 확인 (KSE=코스피, KOSDAQ=코스닥)
        if (!"KSE".equals(meta.getFullExchangeName()) && !"KOSDAQ".equals(meta.getFullExchangeName())) {
            return "3";
        }

        return "";
    }

    /**
     * 기업코드 정보 검색기업정보 검색
     * @param corpCode
     * @return
     */
    private CorpCodeDTO getCorpCode(String corpCode) {

        CorpCodeDTO corpCodeDTO = new CorpCodeDTO();

        CorpCodeResDTO corpCodeResDTO = corpCodeService.getCorpCode(true);
        List<CorpCodeDTO> codeDTOList = new LinkedList<>();
        if(corpCodeResDTO != null || corpCodeResDTO.getList() != null) {
            codeDTOList = corpCodeResDTO.getList();
        }

        corpCodeDTO = codeDTOList.stream()
                .filter(obj -> corpCode.equals(obj.getCorpCode()))
                .findFirst()
                .orElse(new CorpCodeDTO());

        return corpCodeDTO;
    }

    /**
     * 기업 1주당 가치 계산
     * @param corpCode
     * @param year
     * @param fss01 당기
     * @param fss02 전기
     * @param fss03 전전기
     * @param context
     * @return
     */
    private String calCompanyValue(
            String corpCode,
            String year,
            Map<String, FinancialStatementDTO> fss01,
            Map<String, FinancialStatementDTO> fss02,
            Map<String, FinancialStatementDTO> fss03,
            CalculationContext context
    ) {

        int tYear = Integer.parseInt(year); // 기준연도

        //@ 저평가 확인
        //# 사업가치 [영업이익(전전기/전기/당기)]
        String bussValue = calBussValue(fss03, fss02, fss01, context.selFinanceBungiCode, context);
        context.resultDetail.set계산_사업가치(bussValue);

        //# 재산가치 [유동자산, 유동부채, 유동비율, 투자자산(비유동자산내)]
        String properValue = calProperValue(fss01, context);
        context.resultDetail.set계산_재산가치(properValue);

        //# 고정부채(비유동부채)
        String fixedLb = getFixedLiabilities(fss01);
        context.resultDetail.set고정부채(fixedLb);
        context.resultDetail.set계산_부채(fixedLb);

        //# 발행주식수
        String selNoIBungiCode = ReprtCode.사업보고서.getCode();
        NumberOfSharesIssuedResDTO numberOfSharesIssuedResDTO =
                getNumberOfSharesIssued(corpCode, String.valueOf(tYear-1), ReprtCode.사업보고서);

        String totalStockShareIssue = getCommonStockTotalShareIssue(numberOfSharesIssuedResDTO);    // 발행주식수

        if("".equals(totalStockShareIssue)) {
            // 사업보고서가 없는 경우(4분기 발표전 다음해) 작년3분기 조회
            selNoIBungiCode = ReprtCode.분기보고서_3.getCode();
            numberOfSharesIssuedResDTO =
                    getNumberOfSharesIssued(corpCode, String.valueOf(tYear-1), ReprtCode.분기보고서_3);
            totalStockShareIssue = getCommonStockTotalShareIssue(numberOfSharesIssuedResDTO);    // 발행주식수
        }

        if("".equals(totalStockShareIssue) && selNoIBungiCode.equals(ReprtCode.분기보고서_3.getCode())) {
            // 3분기보고서도 없으면 2분기보고서(반기보고서)
            selNoIBungiCode = ReprtCode.반기보고서.getCode();
            numberOfSharesIssuedResDTO =
                    getNumberOfSharesIssued(corpCode, String.valueOf(tYear-1), ReprtCode.반기보고서);
            totalStockShareIssue = getCommonStockTotalShareIssue(numberOfSharesIssuedResDTO);    // 발행주식수
        }

        if("".equals(totalStockShareIssue) && selNoIBungiCode.equals(ReprtCode.반기보고서.getCode())) {
            // 2분기보고서도 없으면 1분기보고서
            selNoIBungiCode = ReprtCode.분기보고서_1.getCode();
            numberOfSharesIssuedResDTO =
                    getNumberOfSharesIssued(corpCode, String.valueOf(tYear-1), ReprtCode.분기보고서_1);
            totalStockShareIssue = getCommonStockTotalShareIssue(numberOfSharesIssuedResDTO);    // 발행주식수
        }

        context.resultDetail.set발행주식수(totalStockShareIssue);

        //# 결과
        // 기업 가치 (사업가치 + 재산가치 - 부채)
        String jujuValue = CalUtil.add(bussValue, properValue);
        jujuValue = CalUtil.sub(jujuValue, fixedLb);
        context.resultDetail.set계산_기업가치(jujuValue);

        // 1주당가치 (기업가치 / 발행주식수)
        String result = CalUtil.divide(jujuValue, totalStockShareIssue, RoundingMode.HALF_UP);

        return result;
    }

    /**
     * 보통주 : 총 주식 발행 수
     * @param numberOfSharesIssuedResDTO
     * @return
     */
    private String getCommonStockTotalShareIssue(NumberOfSharesIssuedResDTO numberOfSharesIssuedResDTO) {

        if(numberOfSharesIssuedResDTO == null || numberOfSharesIssuedResDTO.getList() == null) return "";

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
     * @param corpCode
     * @param year
     * @param reprtCode
     * @return
     */
    private NumberOfSharesIssuedResDTO getNumberOfSharesIssued(String corpCode, String year, ReprtCode reprtCode) {

        NumberOfSharesIssuedResDTO numberOfSharesIssuedResDTO =
                numberOfSharesIssuedService.getNumberOfSharesIssued(corpCode, year, reprtCode.getCode());

        return numberOfSharesIssuedResDTO;
    }

    /**
     * 고정부채(비유동부채)
     * @param fss01
     * @return
     */
    private String getFixedLiabilities(Map<String, FinancialStatementDTO> fss01) {
        final String FiXED_LB_KEY = "ifrs-full_NoncurrentLiabilities";
        FinancialStatementDTO fs = fss01.get(FiXED_LB_KEY);
        return fs.getThstrmAmount();
    }

    /**
     * 재산가치 계산
     * @param fss01
     * @param context
     * @return
     */
    private String calProperValue(
            Map<String, FinancialStatementDTO> fss01, CalculationContext context) {

        // 유동자산 : ifrs-full_CurrentAssets
        final String CA_KEY = "ifrs-full_CurrentAssets";
        FinancialStatementDTO fs01_ca = fss01.get(CA_KEY);
        String caValue = fs01_ca.getThstrmAmount(); // 유동자산
        context.resultDetail.set유동자산합계(caValue);

        // 유동부채 : ifrs-full_CurrentLiabilities
        final String CL_KEY = "ifrs-full_CurrentLiabilities";
        FinancialStatementDTO fs01_cl = fss01.get(CL_KEY);
        String clValue = fs01_cl.getThstrmAmount(); // 유동부채
        context.resultDetail.set유동부채합계(clValue);

        // 유동비율 : (유동자산 / 유동부채)
        String crValue = CalUtil.divide(caValue, clValue, 2, RoundingMode.HALF_UP);
        context.resultDetail.set유동비율(crValue);

        // 투자자산(비유동자산내)
        // 2025.01.21 : 유동/비유동 구분이 명확하지 않거나 둘 다 포함될수있는 항목도 포함됨
        final String IA_KEY_ARR[] = {
                // 1. 지분투자 관련
                "ifrs-full_InvestmentsInSubsidiariesJointVenturesAndAssociates",    // 종속/관계/공동기업 투자
                "ifrs-full_InvestmentInAssociate",                                  // 관계기업투자
                "ifrs-full_InvestmentsInJointVentures",                            // 공동기업투자
                "ifrs-full_InvestmentsInSubsidiaries",                             // 종속기업투자
                "ifrs-full_EquityInvestments",                                      // 지분투자

                // 2. 금융자산 관련
                "ifrs-full_NoncurrentFinancialAssets",                             // 비유동금융자산
                "ifrs-full_OtherNoncurrentFinancialAssets",                        // 기타비유동금융자산
                "ifrs-full_FinancialAssetsAtFairValueThroughProfitOrLoss",         // 당기손익-공정가치측정금융자산
                "ifrs-full_NoncurrentFinancialAssetsMeasuredAtFairValueThroughOtherComprehensiveIncome",  // 기타포괄손익-공정가치측정금융자산
                "dart_LongTermDepositsNotClassifiedAsCashEquivalents",             // 장기예치금(당기손익-공정가치측정금융자산)
                "ifrs-full_AvailableForSaleFinancialAssets",                       // 매도가능금융자산
                "ifrs-full_HeldToMaturityInvestments",                             // 만기보유투자

                // 3. 기타 투자자산
                "ifrs-full_InvestmentProperty",                                    // 투자부동산
                "ifrs-full_OtherInvestments",                                      // 기타투자자산
                "ifrs-full_LongTermInvestments",                                   // 장기투자자산

                // 4. DART 특화 계정과목
                "dart_LongTermTradeAndOtherNonCurrentReceivablesGross",           // 장기매출채권및기타비유동채권총장부금액
                "dart_LongTermDepositsReceived",                                   // 장기예수금
                "dart_LongTermLoansAndReceivables",                               // 장기대여금및수취채권
                "dart_LongTermDeposits"                                           // 장기예치금
        };

        String iaValueSum = "0";    // 투자자산 합계
        for(final String IA_KEY : IA_KEY_ARR) {
            FinancialStatementDTO fs01_ia = fss01.get(IA_KEY);
            if(fs01_ia == null) continue;   // 값이 없을수도 있음
            String iaValue = fs01_ia.getThstrmAmount();
            iaValueSum = CalUtil.add(iaValueSum, iaValue);
        }
        context.resultDetail.set투자자산_비유동자산내(iaValueSum);

        // 유동자산 - (유동부채 * 1.2(유동비율)) + 투자자산
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
     * @param context
     * @return
     */
    private String calBussValue(
            Map<String, FinancialStatementDTO> fss03,
            Map<String, FinancialStatementDTO> fss02,
            Map<String, FinancialStatementDTO> fss01,
            String selBoongiCode,
            CalculationContext context) {

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

        context.resultDetail.set영업이익_전전기(bussPf03);
        context.resultDetail.set영업이익_전기(bussPf02);
        context.resultDetail.set영업이익_당기(bussPf01);

        String sum = CalUtil.add(CalUtil.add(bussPf03, bussPf02), bussPf01);
        String average = CalUtil.divide(sum, "3", 2, RoundingMode.HALF_UP);

        context.resultDetail.set영업이익_합계(sum);
        context.resultDetail.set영업이익_평균(average);

        return CalUtil.multi(average, "10");
    }

    /**
     * 전전기 재무제표 조회
     * @param year
     * @param corpCode
     * @param context
     * @return
     */
    private Map<String, FinancialStatementDTO> getTwoYearsPriorFinancialStatements(
            String year, String corpCode, CalculationContext context) {

        int tYear = Integer.parseInt(year); // 기준연도

        //@ 재무정보 조회
        // 전전기
        FinancialStatementResDTO financialStatementResDTO =
                getFinancialStatement(corpCode, String.valueOf(tYear-3), ReprtCode.사업보고서, context.selFsDiv);
        if(null == financialStatementResDTO.getList()) {
            context.selFsDiv = FsDiv.개별;
            financialStatementResDTO = getFinancialStatement(corpCode, String.valueOf(tYear-3), ReprtCode.사업보고서, context.selFsDiv);
            if(null == financialStatementResDTO.getList()) {
                // 개별도 없으면 해당기업 계산 생략
                return null;
            }
        }
        Map<String, FinancialStatementDTO> fss03 = cleanUpFinancailStatement(financialStatementResDTO);

        return fss03;
    }

    /**
     * 전기
     * @param year
     * @param corpCode
     * @param context
     * @return
     */
    private Map<String, FinancialStatementDTO> getPriorYearFinancialStatements(
            String year, String corpCode, CalculationContext context) {

        int tYear = Integer.parseInt(year); // 기준연도

        //@ 재무정보 조회
        Map<String, FinancialStatementDTO> fss02 =
                cleanUpFinancailStatement(
                        getFinancialStatement(corpCode, String.valueOf(tYear-2), ReprtCode.사업보고서, context.selFsDiv));

        return fss02;
    }

    /**
     * 당기
     * @param year
     * @param corpCode
     * @param context
     * @return
     */
    private Map<String, FinancialStatementDTO> getCurrentYearFinancialStatements(
            String year, String corpCode, CalculationContext context) {

        int tYear = Integer.parseInt(year); // 기준연도

        // 당기
        context.selFinanceBungiCode = ReprtCode.사업보고서.getCode();
        FinancialStatementResDTO fss01Res =
                getFinancialStatement(corpCode, String.valueOf(tYear-1), ReprtCode.사업보고서, context.selFsDiv);

        if(null == fss01Res.getList()) {
            // 사업보고서가 없는 경우(4분기 발표전 다음해) 작년3분기 조회
            fss01Res = getFinancialStatement(corpCode, String.valueOf(tYear-1), ReprtCode.분기보고서_3, context.selFsDiv);
            context.selFinanceBungiCode = ReprtCode.분기보고서_3.getCode();

        } else if(null == fss01Res.getList() && context.selFinanceBungiCode.equals(ReprtCode.분기보고서_3.getCode())) {
            // 3분기보고서도 없으면 2분기보고서(반기보고서)
            fss01Res = getFinancialStatement(corpCode, String.valueOf(tYear-1), ReprtCode.반기보고서, context.selFsDiv);
            context.selFinanceBungiCode = ReprtCode.반기보고서.getCode();
        }

        Map<String, FinancialStatementDTO> fss01 = cleanUpFinancailStatement(fss01Res);

        return fss01;
    }

    /**
     * 제무제표 조회
     * @param corpCode
     * @param year
     * @param reprtCode
     * @param fsDiv
     * @return
     */
    private FinancialStatementResDTO getFinancialStatement(String corpCode, String year, ReprtCode reprtCode, FsDiv fsDiv) {

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        FinancialStatementResDTO financialStatementResDTO = financialStatmentService.getCompanyFinancialStatement(
                corpCode, String.valueOf(year), reprtCode.getCode(), fsDiv.getCode());

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
