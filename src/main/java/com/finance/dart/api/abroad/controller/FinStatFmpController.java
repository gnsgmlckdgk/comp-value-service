package com.finance.dart.api.abroad.controller;

import com.finance.dart.api.abroad.dto.fmp.balancesheet.BalanceSheetReqDto;
import com.finance.dart.api.abroad.dto.fmp.balancesheet.BalanceSheetResDto;
import com.finance.dart.api.abroad.dto.fmp.enterprisevalues.EnterpriseValuesReqDto;
import com.finance.dart.api.abroad.dto.fmp.enterprisevalues.EnterpriseValuesResDto;
import com.finance.dart.api.abroad.dto.fmp.financialgrowth.FinancialGrowthReqDto;
import com.finance.dart.api.abroad.dto.fmp.financialgrowth.FinancialGrowthResDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosReqDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosResDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosTTM_ReqDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosTTM_ResDto;
import com.finance.dart.api.abroad.dto.fmp.forexquote.ForexQuoteReqDto;
import com.finance.dart.api.abroad.dto.fmp.forexquote.ForexQuoteResDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatement.IncomeStatReqDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatement.IncomeStatResDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatgrowth.IncomeStatGrowthReqDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatgrowth.IncomeStatGrowthResDto;
import com.finance.dart.api.abroad.dto.fmp.keymetrics.KeyMetricsReqDto;
import com.finance.dart.api.abroad.dto.fmp.keymetrics.KeyMetricsResDto;
import com.finance.dart.api.abroad.dto.fmp.quote.AfterTradeReqDto;
import com.finance.dart.api.abroad.dto.fmp.quote.AfterTradeResDto;
import com.finance.dart.api.abroad.dto.fmp.quote.StockQuoteReqDto;
import com.finance.dart.api.abroad.dto.fmp.quote.StockQuoteResDto;
import com.finance.dart.api.abroad.dto.fmp.ratiosttm.RatiosTtmReqDto;
import com.finance.dart.api.abroad.dto.fmp.ratiosttm.RatiosTtmResDto;
import com.finance.dart.api.abroad.dto.fmp.stockscreener.StockScreenerReqDto;
import com.finance.dart.api.abroad.dto.fmp.stockscreener.StockScreenerResDto;
import com.finance.dart.api.abroad.service.fmp.*;
import com.finance.dart.common.dto.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("abroad/company/financial/fmp")
public class FinStatFmpController {

    private final IncomeStatementService incomeStatementService;
    private final BalanceSheetStatementService balanceSheetStatementService;
    private final KeyMetricsService keyMetricsService;
    private final EnterpriseValueService enterpriseValueService;
    private final FinancialRatiosService financialRatiosService;
    private final FinancialGrowthService financialGrowthService;
    private final IncomeStatGrowthService incomeStatGrowthService;
    private final ForexQuoteService forexQuoteService;
    private final StockQuoteService stockQuoteService;                          // 주식 시세 조회 서비스
    private final AfterTradeService afterTradeService;                          // 애프터마켓 시세 조회 서비스
    private final StockScreenerService stockScreenerService;                    // 주식 스크리너 서비스
    private final RatiosTtmService ratiosTtmService;                            // 최근 12개월 재무 비율 조회 서비스


    /**
     * 영업이익 조회
     * @param requestDto
     * @return
     */
    @PostMapping("/incomeStat")
    public ResponseEntity<CommonResponse<List<IncomeStatResDto>>> incomeStatement(@RequestBody IncomeStatReqDto requestDto) {

        List<IncomeStatResDto> response = incomeStatementService.findIncomeStat(requestDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 재무상태표 조회
     * @param requestDto
     * @return
     */
    @PostMapping("/balanceSheetStat")
    public ResponseEntity<CommonResponse<List<BalanceSheetResDto>>> balanceSheetStat(@RequestBody BalanceSheetReqDto requestDto) {

        List<BalanceSheetResDto> response = balanceSheetStatementService.findBalanceSheet(requestDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 주요 재무지표 조회
     * @param requestDto
     * @return
     */
    @PostMapping("/keyMetrics")
    public ResponseEntity<CommonResponse<List<KeyMetricsResDto>>> keyMetrics(@RequestBody KeyMetricsReqDto requestDto) {

        List<KeyMetricsResDto> response = keyMetricsService.findKeyMetrics(requestDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 기업가치 조회
     * @param requestDto
     * @return
     */
    @PostMapping("/enterpriseValues")
    public ResponseEntity<CommonResponse<List<EnterpriseValuesResDto>>> enterpriseValues(@RequestBody EnterpriseValuesReqDto requestDto) {

        List<EnterpriseValuesResDto> response = enterpriseValueService.findEnterpriseValue(requestDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 재무비율지표 조회
     * @param requestDto
     * @return
     */
    @PostMapping("/financialRatios")
    public ResponseEntity<CommonResponse<List<FinancialRatiosResDto>>> financialRatios(@RequestBody FinancialRatiosReqDto requestDto) {

        List<FinancialRatiosResDto> response = financialRatiosService.findFinancialRatios(requestDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 재무비율지표 TTM 조회
     * @param requestDto
     * @return
     */
    @PostMapping("/financialRatiosTTM")
    public ResponseEntity<CommonResponse<List<FinancialRatiosTTM_ResDto>>> financialRatiosTTM(@RequestBody FinancialRatiosTTM_ReqDto requestDto) {

        List<FinancialRatiosTTM_ResDto> response = financialRatiosService.findFinancialRatiosTTM(requestDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 성장률 조회
     * @param requestDto
     * @return
     */
    @PostMapping("/financialGrowth")
    public ResponseEntity<CommonResponse<List<FinancialGrowthResDto>>> financialGrowth(@RequestBody FinancialGrowthReqDto requestDto) {

        List<FinancialGrowthResDto> response = financialGrowthService.financialStatementsGrowth(requestDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 영업 성장률 조회
     * @param requestDto
     * @return
     */
    @PostMapping("/IncomeStatGrowth")
    public ResponseEntity<CommonResponse<List<IncomeStatGrowthResDto>>> incomeStatementGrowth(@RequestBody IncomeStatGrowthReqDto requestDto) {

        List<IncomeStatGrowthResDto> response = incomeStatGrowthService.findIncomeStatGrowth(requestDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 외환시세(환율) 조회
     * @param requestDto
     * @return
     */
    @PostMapping("/forexQuote")
    public ResponseEntity<CommonResponse<List<ForexQuoteResDto>>> forexQuote(@RequestBody ForexQuoteReqDto requestDto) {

        List<ForexQuoteResDto> response = forexQuoteService.findForexQuote(requestDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * <pre>
     * 주식시세 조회
     * 정규장
     * forexQuote 간소화 버전
     * </pre>
     * @param requestDto
     * @return
     */
    @PostMapping("/stockQuote")
    public ResponseEntity<CommonResponse<List<StockQuoteResDto>>> stockQuote(@RequestBody StockQuoteReqDto requestDto) {

        List<StockQuoteResDto> response = stockQuoteService.findStockQuote(requestDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 애프터마켓 시세 조회
     * @param requestDto
     * @return
     */
    @PostMapping("/afterTrade")
    public ResponseEntity<CommonResponse<List<AfterTradeResDto>>> afterTrade(@RequestBody AfterTradeReqDto requestDto) {

        List<AfterTradeResDto> response = afterTradeService.findAfterTrade(requestDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 주식 스크리너 API
     * @param requestDto
     * @return
     */
    @PostMapping("/screener/stock")
    public ResponseEntity<CommonResponse<List<StockScreenerResDto>>> screenerStock(@RequestBody StockScreenerReqDto requestDto) {

        List<StockScreenerResDto> response = stockScreenerService.findStockScreener(requestDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 최근 12개월 재무 비율 조회
     * @param requestDto
     * @return
     */
    @PostMapping("/ratios/ttm")
    public ResponseEntity<CommonResponse<List<RatiosTtmResDto>>> ratiosTTM(@RequestBody RatiosTtmReqDto requestDto) {

        List<RatiosTtmResDto> response = ratiosTtmService.findRatiosTTM(requestDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }
}
