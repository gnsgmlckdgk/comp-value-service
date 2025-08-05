package com.finance.dart.api.domestic.controller;

import com.finance.dart.api.domestic.dto.FinancialStatementResDTO;
import com.finance.dart.api.domestic.service.FinancialStatementService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 정기보고서 재무정보 컨트롤러
 */

@RestController
@AllArgsConstructor
@RequestMapping("financial")
public class FinancialStatementController {

    private final FinancialStatementService financialStatementService;

    /**
     * 단일회사 전체 재무제표 조회
     * @param corpCode  고유번호
     * @param bsnsYear  사업연도
     * @param reprtCode 보고서코드
     * @param fsDiv     개별/연결구분
     * @return
     */
    @GetMapping("/company/financial-statement")
    public ResponseEntity<FinancialStatementResDTO> getCompanyFinancialStatement(
            @RequestParam("corp_code") String corpCode,
            @RequestParam("bsns_year") String bsnsYear,
            @RequestParam("reprt_code") String reprtCode,
            @RequestParam("fs_div") String fsDiv
    ) {

        FinancialStatementResDTO financialStatementResDTO =
                financialStatementService.getCompanyFinancialStatement(corpCode, bsnsYear, reprtCode, fsDiv);

        return new ResponseEntity<>(financialStatementResDTO, HttpStatus.OK);
    }

}
