package com.finance.dart.api.abroad.controller;

import com.finance.dart.api.abroad.dto.fmp.balancesheet.BalanceSheetReqDto;
import com.finance.dart.api.abroad.dto.fmp.balancesheet.BalanceSheetResDto;
import com.finance.dart.api.abroad.dto.fmp.enterprisevalues.EnterpriseValuesReqDto;
import com.finance.dart.api.abroad.dto.fmp.enterprisevalues.EnterpriseValuesResDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosReqDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosResDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosTTM_ReqDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosTTM_ResDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatement.IncomeStatReqDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatement.IncomeStatResDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatgrowth.IncomeStatGrowthReqDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatgrowth.IncomeStatGrowthResDto;
import com.finance.dart.api.abroad.dto.fmp.keymetrics.KeyMetricsReqDto;
import com.finance.dart.api.abroad.dto.fmp.keymetrics.KeyMetricsResDto;
import com.finance.dart.api.abroad.service.fmp.FinancialRatiosService;
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
    private final IncomeStatGrowthService incomeStatGrowthService;


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
     * 재무제표 성장률 조회
     * @param requestDto
     * @return
     */
    @PostMapping("/IncomeStatGrowth")
    public ResponseEntity<CommonResponse<List<IncomeStatGrowthResDto>>> incomeStatementGrowth(@RequestBody IncomeStatGrowthReqDto requestDto) {

        List<IncomeStatGrowthResDto> response = incomeStatGrowthService.findIncomeStatGrowth(requestDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

}
