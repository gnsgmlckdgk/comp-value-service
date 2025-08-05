package com.finance.dart.api.domestic.service;

import com.finance.dart.api.common.service.NumberOfSharesIssuedService;
import com.finance.dart.api.domestic.dto.*;
import com.finance.dart.api.domestic.enums.ExchangeCd;
import com.finance.dart.api.domestic.enums.FsDiv;
import com.finance.dart.api.domestic.enums.ReprtCode;
import com.finance.dart.common.service.HttpClientService;
import com.finance.dart.common.util.CalUtil;
import com.finance.dart.common.util.ClientUtil;
import com.finance.dart.common.util.DateUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 한 기업의 주당 가치 계산
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CalCompanyStockPerValueService {

    private static final String EXEC_ACCT_ID = "-표준계정코드 미사용-";

    private final CorpCodeService corpCodeService;
    private final FinancialStatementService financialStatementService;
    private final NumberOfSharesIssuedService numberOfSharesIssuedService;
    private final HttpClientService httpClientService;

    @Getter
    public static class CalculationContext {
        private FsDiv selectedFsDiv;
        private String selectedFinanceBungiCode;
        private final StockValueResultDetailDTO resultDetail;

        public CalculationContext() {
            this.selectedFsDiv = FsDiv.연결; // 기본값
            this.selectedFinanceBungiCode = ReprtCode.사업보고서.getCode();
            this.resultDetail = new StockValueResultDetailDTO();
        }
    }

    /**
     * 기업의 한 주당 가치를 계산한다.
     *
     * @param year     기준 연도
     * @param corpCode 기업 코드
     * @param corpName 기업 명
     * @return 계산 결과 DTO
     */
    public StockValueResultDTO calPerValue(String year, String corpCode, String corpName) throws InterruptedException {
        final CalculationContext context = new CalculationContext();
        StockValueResultDTO result = new StockValueResultDTO("정상 처리되었습니다.");

        // 1. 회사 정보 설정
        CorpCodeDTO corpCodeDTO = setCompanyInfo(corpCode, corpName);
        if (corpCodeDTO == null) {
            return setReturnMessage(result, "회사정보가 존재하지 않습니다.");
        }
        corpCode = corpCodeDTO.getCorpCode();
        result.set기업코드(corpCodeDTO.getCorpCode());
        result.set기업명(corpCodeDTO.getCorpName());
        result.set주식코드(corpCodeDTO.getStockCode());

        // 2. 재무제표 조회 (전전기, 전기, 당기)
        Map<String, FinancialStatementDTO> fss03 = getTwoYearsPriorFinancialStatements(year, corpCode, context);
        if (fss03 == null) {
            String errMessage = "전전기 재무제표 정보가 존재하지 않습니다.";
            result.set결과메시지(errMessage);
            return setReturnMessage(result, errMessage);
        }
        Thread.sleep(500); // 0.5초 딜레이
        Map<String, FinancialStatementDTO> fss02 = getPriorYearFinancialStatements(year, corpCode, context);
        if (fss02 == null) {
            String errMessage = "전기 재무제표 정보가 존재하지 않습니다.";
            result.set결과메시지(errMessage);
            return setReturnMessage(result, errMessage);
        }
        Thread.sleep(500); // 0.5초 딜레이
        Map<String, FinancialStatementDTO> fss01 = getCurrentYearFinancialStatements(year, corpCode, context);
        if (fss01 == null) {
            String errMessage = "당기 재무제표 정보가 존재하지 않습니다.";
            result.set결과메시지(errMessage);
            return setReturnMessage(result, errMessage);
        }

        // 3. 한 주당 가치 계산
        String perShareValue = calCompanyValue(corpCode, year, fss01, fss02, fss03, context);
        if("0".equals(perShareValue)) {
            result.set결과메시지(StringUtils.defaultString(context.getResultDetail().get예외메시지_발행주식수()));
        }
        result.set주당가치(perShareValue);
        result.set상세정보(context.getResultDetail());

        // 4. 현재 가격 조회
        String currentStockPrice = getCurrentValue(corpCodeDTO.getStockCode(), ExchangeCd.코스피.getCode(), false);
        result.set현재가격(currentStockPrice);

        // 5. 확인 시간 설정
        result.set확인시간(DateUtil.getToday("yyyy-MM-dd HH:mm:ss"));

        return result;
    }

    private StockValueResultDTO setReturnMessage(StockValueResultDTO result, String message) {
        result.set주당가치("");
        result.set결과메시지(message);
        return result;
    }

    private CorpCodeDTO setCompanyInfo(String corpCode, String corpName) {
        if (corpCode.isEmpty() && !corpName.isEmpty()) {
            CorpCodeDTO dto = corpCodeService.getCorpCodeFindName(true, corpName);
            return dto;
        } else {
            return getCorpCode(corpCode);
        }
    }

    private String getCurrentValue(String stockCode, String exchangeCd, boolean isReSelect) {
        final String symbol = stockCode + "." + exchangeCd;
        final String url = "https://query1.finance.yahoo.com/v8/finance/chart/" + symbol;

        Map<String, String> headersData = new LinkedHashMap<>();
        headersData.put("User-Agent", "Mozilla/5.0");

        try {
            ResponseEntity<StockPriceDTO> response = httpClientService.exchangeSync(
                    url, HttpMethod.GET,
                    ClientUtil.createHttpEntity(MediaType.APPLICATION_JSON, headersData),
                    StockPriceDTO.class
            );

            try {
                Thread.sleep(1000); // 과도한 요청방지
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if(response == null || response.getBody() == null) return "";

            StockPriceDTO.Meta meta = response.getBody().getChart().getResult().get(0).getMeta();
            String validation = validationResponse(meta);
            if (validation.isEmpty()) {
                return String.valueOf(Math.round(meta.getRegularMarketPrice()));
            } else if ("1".equals(validation)) {
                // 재시도: 코스피와 코스닥을 교차 조회
                if(!isReSelect) {   // 재조회가 아닌경우
                    if (ExchangeCd.코스피.getCode().equals(exchangeCd)) {
                        return getCurrentValue(stockCode, ExchangeCd.코스닥.getCode(), true);
                    } else if (ExchangeCd.코스닥.getCode().equals(exchangeCd)) {
                        return getCurrentValue(stockCode, ExchangeCd.코스피.getCode(), true);
                    }
                }
            }

        } catch(Exception e) {
            log.error("현재 가격 조회 중 예외 발생 = {}", e.getMessage());
            return "";
        }

        return "";
    }

    private String validationResponse(StockPriceDTO.Meta meta) {
        if (!"EQUITY".equals(meta.getInstrumentType())) {
            return "1";
        }
        if (meta.getRegularMarketPrice() == null || meta.getRegularMarketPrice() <= 0) {
            return "2";
        }
        if (!ExchangeCd.코스피.getFullExchangeName().equals(meta.getFullExchangeName())
                && !ExchangeCd.코스닥.getFullExchangeName().equals(meta.getFullExchangeName())) {
            return "3";
        }
        return "";
    }

    private CorpCodeDTO getCorpCode(String corpCode) {
        CorpCodeResDTO res = corpCodeService.getCorpCode(true);
        List<CorpCodeDTO> list = (res != null && res.getList() != null) ? res.getList() : new LinkedList<>();
        return list.stream()
                .filter(dto -> corpCode.equals(dto.getCorpCode()))
                .findFirst()
                .orElse(new CorpCodeDTO());
    }

    private String calCompanyValue(String corpCode, String year,
                                   Map<String, FinancialStatementDTO> fss01,
                                   Map<String, FinancialStatementDTO> fss02,
                                   Map<String, FinancialStatementDTO> fss03,
                                   CalculationContext context) {
        int tYear = Integer.parseInt(year);

        // 사업가치 계산
        String businessValue = calBussValue(fss03, fss02, fss01, context.selectedFinanceBungiCode, context);
        context.resultDetail.set계산_사업가치(businessValue);

        // 재산가치 계산
        String properValue = calProperValue(fss01, context);
        context.resultDetail.set계산_재산가치(properValue);

        // 고정부채
        String fixedLiabilities = getFixedLiabilities(fss01);
        context.resultDetail.set고정부채(fixedLiabilities);
        context.resultDetail.set계산_부채(fixedLiabilities);

        // 발행주식수 조회
        String selectedReportCode = ReprtCode.사업보고서.getCode();
        NumberOfSharesIssuedResDTO sharesRes = getNumberOfSharesIssued(corpCode, String.valueOf(tYear - 1), ReprtCode.사업보고서);
        String totalShares = getCommonStockTotalShareIssue(sharesRes);
        if (totalShares.isEmpty()) {
            selectedReportCode = ReprtCode.분기보고서_3.getCode();
            sharesRes = getNumberOfSharesIssued(corpCode, String.valueOf(tYear - 1), ReprtCode.분기보고서_3);
            totalShares = getCommonStockTotalShareIssue(sharesRes);
            context.resultDetail.set예외메시지_발행주식수("당기 보고서 발행주식수 정보가 없어서 3분기 발행주식수 정보 조회");
        }
        if (totalShares.isEmpty() && selectedReportCode.equals(ReprtCode.분기보고서_3.getCode())) {
            selectedReportCode = ReprtCode.반기보고서.getCode();
            sharesRes = getNumberOfSharesIssued(corpCode, String.valueOf(tYear - 1), ReprtCode.반기보고서);
            totalShares = getCommonStockTotalShareIssue(sharesRes);
            context.resultDetail.set예외메시지_발행주식수("3분기 보고서 발행주식수 정보가 없어서 2분기 발행주식수 정보 조회");
        }
        if (totalShares.isEmpty() && selectedReportCode.equals(ReprtCode.반기보고서.getCode())) {
            selectedReportCode = ReprtCode.분기보고서_1.getCode();
            sharesRes = getNumberOfSharesIssued(corpCode, String.valueOf(tYear - 1), ReprtCode.분기보고서_1);
            totalShares = getCommonStockTotalShareIssue(sharesRes);
            context.resultDetail.set예외메시지_발행주식수("2분기 보고서 발행주식수 정보가 없어서 1분기 발행주식수 정보 조회");
        }

        if (totalShares.isEmpty() && selectedReportCode.equals(ReprtCode.분기보고서_1.getCode())) {
            context.resultDetail.set예외메시지_발행주식수("1분기 보고서 발행주식수 정보가 없어서 계산 불가");
            return "0";
        }

        context.resultDetail.set발행주식수(totalShares);

        // 기업 가치 및 한 주당 가치 계산
        String companyValue = CalUtil.sub(CalUtil.add(businessValue, properValue), fixedLiabilities);
        context.resultDetail.set계산_기업가치(companyValue);
        return CalUtil.divide(companyValue, totalShares, RoundingMode.HALF_UP);
    }

    private String getCommonStockTotalShareIssue(NumberOfSharesIssuedResDTO resDTO) {
        if (resDTO == null || resDTO.getList() == null) return "";
        for (NumberOfSharesIssuedDTO dto : resDTO.getList()) {
            if ("보통주".equals(dto.getSe())) {
                return dto.getIstc_totqy();
            }
        }
        return "";
    }

    private NumberOfSharesIssuedResDTO getNumberOfSharesIssued(String corpCode, String year, ReprtCode reportCode) {
        return numberOfSharesIssuedService.getNumberOfSharesIssued(corpCode, year, reportCode.getCode());
    }

    private String getFixedLiabilities(Map<String, FinancialStatementDTO> fss01) {
        final String FIXED_LB_KEY = "ifrs-full_NoncurrentLiabilities";
        FinancialStatementDTO fs = fss01.get(FIXED_LB_KEY);
        return fs.getThstrmAmount();
    }

    private String calProperValue(Map<String, FinancialStatementDTO> fss01, CalculationContext context) {
        final String CA_KEY = "ifrs-full_CurrentAssets";
        final String CL_KEY = "ifrs-full_CurrentLiabilities";

        FinancialStatementDTO assetsDTO = fss01.get(CA_KEY);
        String currentAssets = assetsDTO.getThstrmAmount();
        context.resultDetail.set유동자산합계(currentAssets);

        FinancialStatementDTO liabilitiesDTO = fss01.get(CL_KEY);
        String currentLiabilities = liabilitiesDTO.getThstrmAmount();
        context.resultDetail.set유동부채합계(currentLiabilities);

        String currentRatio = CalUtil.divide(currentAssets, currentLiabilities, 2, RoundingMode.HALF_UP);
        context.resultDetail.set유동비율(currentRatio);

        // 투자자산(비유동자산내) 합산
        final String[] IA_KEYS = {
                "ifrs-full_InvestmentsInSubsidiariesJointVenturesAndAssociates",
                "ifrs-full_InvestmentInAssociate",
                "ifrs-full_InvestmentsInJointVentures",
                "ifrs-full_InvestmentsInSubsidiaries",
                "ifrs-full_EquityInvestments",
                "ifrs-full_NoncurrentFinancialAssets",
                "ifrs-full_OtherNoncurrentFinancialAssets",
                "ifrs-full_FinancialAssetsAtFairValueThroughProfitOrLoss",
                "ifrs-full_NoncurrentFinancialAssetsMeasuredAtFairValueThroughOtherComprehensiveIncome",
                "dart_LongTermDepositsNotClassifiedAsCashEquivalents",
                "ifrs-full_AvailableForSaleFinancialAssets",
                "ifrs-full_HeldToMaturityInvestments",
                "ifrs-full_InvestmentProperty",
                "ifrs-full_OtherInvestments",
                "ifrs-full_LongTermInvestments",
                "dart_LongTermTradeAndOtherNonCurrentReceivablesGross",
                "dart_LongTermDepositsReceived",
                "dart_LongTermLoansAndReceivables",
                "dart_LongTermDeposits"
        };

        String investmentSum = "0";
        for (String key : IA_KEYS) {
            FinancialStatementDTO dto = fss01.get(key);
            if (dto == null || dto.getThstrmAmount().isEmpty()) continue;
            investmentSum = CalUtil.add(investmentSum, dto.getThstrmAmount());
        }
        context.resultDetail.set투자자산_비유동자산내(investmentSum);

        String liabilitiesMultiplied = CalUtil.multi(currentLiabilities, currentRatio);
        String assetDiff = CalUtil.sub(currentAssets, liabilitiesMultiplied);
        return CalUtil.add(assetDiff, investmentSum);
    }

    private String calBussValue(Map<String, FinancialStatementDTO> fss03,
                                Map<String, FinancialStatementDTO> fss02,
                                Map<String, FinancialStatementDTO> fss01,
                                String reportCode,
                                CalculationContext context) {
        final String BUSS_KEY = "dart_OperatingIncomeLoss";

        FinancialStatementDTO fs03 = fss03.get(BUSS_KEY);
        FinancialStatementDTO fs02 = fss02.get(BUSS_KEY);
        FinancialStatementDTO fs01 = fss01.get(BUSS_KEY);

        String opIncomePrevPrev = fs03.getThstrmAmount();
        String opIncomePrev = fs02.getThstrmAmount();
        String opIncomeCurrent = fs01.getThstrmAmount();

        if (reportCode.equals(ReprtCode.분기보고서_3.getCode())) {
            opIncomeCurrent = CalUtil.add(opIncomeCurrent, fs01.getThstrmAddAmount());
            context.resultDetail.set예외메세지_영업이익("당기 영업이익정보 없어서 3분기 영업이익 정보 한번 더 합산");
        } else if (reportCode.equals(ReprtCode.반기보고서.getCode())) {
            opIncomeCurrent = CalUtil.add(opIncomeCurrent, fs01.getThstrmAddAmount());
            opIncomeCurrent = CalUtil.add(opIncomeCurrent, fs01.getThstrmAmount());
            context.resultDetail.set예외메세지_영업이익("3분기 영업이익정보 없어서 2분기 영업이익 정보 두번 더 합산");
        }

        context.resultDetail.set영업이익_전전기(opIncomePrevPrev);
        context.resultDetail.set영업이익_전기(opIncomePrev);
        context.resultDetail.set영업이익_당기(opIncomeCurrent);

        String sum = CalUtil.add(CalUtil.add(opIncomePrevPrev, opIncomePrev), opIncomeCurrent);
        String average = CalUtil.divide(sum, "3", 2, RoundingMode.HALF_UP);
        context.resultDetail.set영업이익_합계(sum);
        context.resultDetail.set영업이익_평균(average);

        return CalUtil.multi(average, "10");
    }

    private Map<String, FinancialStatementDTO> getTwoYearsPriorFinancialStatements(String year, String corpCode, CalculationContext context) {
        int tYear = Integer.parseInt(year);
        FinancialStatementResDTO resDTO = getFinancialStatement(corpCode, String.valueOf(tYear - 3), ReprtCode.사업보고서, context.selectedFsDiv);
        if (resDTO.getList() == null) {
            context.selectedFsDiv = FsDiv.개별;
            resDTO = getFinancialStatement(corpCode, String.valueOf(tYear - 3), ReprtCode.사업보고서, context.selectedFsDiv);
            if (resDTO.getList() == null) {
                return null;
            }
        }
        return cleanUpFinancialStatement(resDTO);
    }

    private Map<String, FinancialStatementDTO> getPriorYearFinancialStatements(String year, String corpCode, CalculationContext context) {
        int tYear = Integer.parseInt(year);
        FinancialStatementResDTO resDTO = getFinancialStatement(corpCode, String.valueOf(tYear - 2), ReprtCode.사업보고서, context.selectedFsDiv);
        if(resDTO.getList() == null) {
            context.selectedFsDiv = FsDiv.개별;
            resDTO = getFinancialStatement(corpCode, String.valueOf(tYear - 2), ReprtCode.사업보고서, context.selectedFsDiv);
            if (resDTO.getList() == null) {
                return null;
            }
        }
        return cleanUpFinancialStatement(resDTO);
    }

    private Map<String, FinancialStatementDTO> getCurrentYearFinancialStatements(String year, String corpCode, CalculationContext context) {
        int tYear = Integer.parseInt(year);
        context.selectedFinanceBungiCode = ReprtCode.사업보고서.getCode();
        FinancialStatementResDTO resDTO = getFinancialStatement(corpCode, String.valueOf(tYear - 1), ReprtCode.사업보고서, context.selectedFsDiv);
        if (resDTO.getList() == null) {
            resDTO = getFinancialStatement(corpCode, String.valueOf(tYear - 1), ReprtCode.분기보고서_3, context.selectedFsDiv);
            context.selectedFinanceBungiCode = ReprtCode.분기보고서_3.getCode();
        } else if (resDTO.getList() == null && context.selectedFinanceBungiCode.equals(ReprtCode.분기보고서_3.getCode())) {
            resDTO = getFinancialStatement(corpCode, String.valueOf(tYear - 1), ReprtCode.반기보고서, context.selectedFsDiv);
            context.selectedFinanceBungiCode = ReprtCode.반기보고서.getCode();
        }
        return cleanUpFinancialStatement(resDTO);
    }

    private FinancialStatementResDTO getFinancialStatement(String corpCode, String year, ReprtCode reportCode, FsDiv fsDiv) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return financialStatementService.getCompanyFinancialStatement(corpCode, year, reportCode.getCode(), fsDiv.getCode());
    }

    private Map<String, FinancialStatementDTO> cleanUpFinancialStatement(FinancialStatementResDTO resDTO) {
        List<FinancialStatementDTO> list = resDTO.getList();
        Map<String, FinancialStatementDTO> cleaned = new LinkedHashMap<>();
        for (FinancialStatementDTO dto : list) {
            if (EXEC_ACCT_ID.equals(dto.getAccountId())) continue;
            cleaned.put(dto.getAccountId(), dto);
        }
        return cleaned;
    }
}