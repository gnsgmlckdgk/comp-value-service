package com.finance.dart.api.domestic.controller;

import com.finance.dart.api.domestic.dto.NumberOfSharesIssuedResDTO;
import com.finance.dart.api.common.service.NumberOfSharesIssuedService;
import com.finance.dart.common.logging.TransactionLogging;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 정기보고서 주요정보 컨트롤러
 */

@RestController
@AllArgsConstructor
@RequestMapping("major")
public class MajorStatementController {

    private final NumberOfSharesIssuedService numberOfSharesIssuedService;

    /**
     * 주식의 총 수 현황 조회
     * @param corpCode
     * @param bsnsYear
     * @param reprtCode
     * @return¢¢
     */
    @TransactionLogging
    @GetMapping("/company/statement")
    public ResponseEntity<NumberOfSharesIssuedResDTO> getNumberOfSharesIssued(
            @RequestParam("corp_code") String corpCode,
            @RequestParam("bsns_year") String bsnsYear,
            @RequestParam("reprt_code") String reprtCode
    ) {

        NumberOfSharesIssuedResDTO numberOfSharesIssuedResDTO =
                numberOfSharesIssuedService.getNumberOfSharesIssued(corpCode, bsnsYear, reprtCode);

        return new ResponseEntity<>(numberOfSharesIssuedResDTO, HttpStatus.OK);
    }


}