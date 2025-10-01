package com.finance.dart.api.abroad.controller;

import com.finance.dart.api.abroad.dto.fmp.balancesheet.BalanceSheetReqDto;
import com.finance.dart.api.abroad.dto.fmp.balancesheet.BalanceSheetResDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatement.IncomeStatReqDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatement.IncomeStatResDto;
import com.finance.dart.api.abroad.dto.fmp.keymetrics.KeyMetricsReqDto;
import com.finance.dart.api.abroad.dto.fmp.keymetrics.KeyMetricsResDto;
import com.finance.dart.api.abroad.service.fmp.BalanceSheetStatementService;
import com.finance.dart.api.abroad.service.fmp.IncomeStatementService;
import com.finance.dart.api.abroad.service.fmp.KeyMetricsService;
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


}
