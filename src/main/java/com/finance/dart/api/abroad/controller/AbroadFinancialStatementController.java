package com.finance.dart.api.abroad.controller;

import com.finance.dart.api.abroad.dto.financial.statement.AssetsCurrent;
import com.finance.dart.api.abroad.dto.financial.statement.OperatingIncomeLossDto;
import com.finance.dart.api.abroad.service.AbroadFinancialStatementService;
import com.finance.dart.common.dto.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("abroad/company/financial")
public class AbroadFinancialStatementController {

    private final AbroadFinancialStatementService abroadFinancialStatementService;


    /**
     * 영업이익 조회
     * @param cik
     * @return
     */
    @GetMapping("/statement/detail/operating-income-loss")
    public ResponseEntity<CommonResponse<OperatingIncomeLossDto>> findFS_OperatingIncomeLoss(@RequestParam(name = "cik") String cik) {

        OperatingIncomeLossDto response =
                abroadFinancialStatementService.findFS_OperatingIncomeLoss(cik);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 유동자산 합계 조회
     * @param cik
     * @return
     */
    @GetMapping("/statement/detail/assets-current")
    public ResponseEntity<CommonResponse<AssetsCurrent>> findFS_AssetsCurrent(@RequestParam(name = "cik") String cik) {

        AssetsCurrent response =
                abroadFinancialStatementService.findFS_AssetsCurrent(cik);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

}
