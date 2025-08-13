package com.finance.dart.api.abroad.controller;

import com.finance.dart.api.abroad.dto.financial.statement.CommonFinancialStatementDto;
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

import java.util.LinkedHashMap;
import java.util.Map;

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
    @GetMapping("/statement/detail/operatingIncomeLoss")
    public ResponseEntity<CommonResponse<CommonFinancialStatementDto>> findFS_OperatingIncomeLoss(@RequestParam(name = "cik") String cik) {

        CommonFinancialStatementDto response =
                abroadFinancialStatementService.findFS_OperatingIncomeLoss(cik);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 유동자산 합계 조회
     * @param cik
     * @return
     */
    @GetMapping("/statement/detail/assetsCurrent")
    public ResponseEntity<CommonResponse<CommonFinancialStatementDto>> findFS_AssetsCurrent(@RequestParam(name = "cik") String cik) {

        CommonFinancialStatementDto response =
                abroadFinancialStatementService.findFS_AssetsCurrent(cik);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 유동부채 합계 조회
     * @param cik
     * @return
     */
    @GetMapping("/statement/detail/liabilitiesCurrent")
    public ResponseEntity<CommonResponse<CommonFinancialStatementDto>> findFS_LiabilitiesCurrent(@RequestParam(name = "cik") String cik) {

        CommonFinancialStatementDto response =
                abroadFinancialStatementService.findFS_LiabilitiesCurrent(cik);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }


    /**
     * 비유동자산내 투자자산 조회
     * @param cik
     * @return
     */
    @GetMapping("/statement/detail/noncurrentInvestments")
    public ResponseEntity<CommonResponse<Map<String, CommonFinancialStatementDto>>> findFS_NoncurrentInvestments(@RequestParam(name = "cik") String cik) {

        Map<String, CommonFinancialStatementDto> response = new LinkedHashMap<>();

        //@1. 장기매도 가능 증권
        CommonFinancialStatementDto response1 =
                abroadFinancialStatementService.findFS_AvailableForSaleSecuritiesNoncurrent(cik);
        response.put("1. 장기매도 가능 증권", response1);

        //@2. 지분법 투자
        CommonFinancialStatementDto response2 =
                abroadFinancialStatementService.findFS_LongTermInvestments(cik);
        response.put("2. 지분법 투자", response2);

        //@3. 기타 장기투자
        CommonFinancialStatementDto response3 =
                abroadFinancialStatementService.findFS_OtherInvestments(cik);
        response.put("3. 기타 장기투자", response3);

        //@4. 투자 및 대여금
        CommonFinancialStatementDto response4 =
                abroadFinancialStatementService.findFS_InvestmentsAndAdvances(cik);
        response.put("4. 투자 및 대여금", response4);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 고정부채 합계 조회
     * @param cik
     * @return
     */
    @GetMapping("/statement/detail/LiabilitiesNoncurrent")
    public ResponseEntity<CommonResponse<CommonFinancialStatementDto>> findFS_LiabilitiesNoncurrent(@RequestParam(name = "cik") String cik) {

        CommonFinancialStatementDto response =
                abroadFinancialStatementService.findFS_LiabilitiesNoncurrent(cik);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 발행주식수 조회
     * @param cik
     * @return
     */
    @GetMapping("/statement/detail/entityCommonStockSharesOutstanding")
    public ResponseEntity<CommonResponse<CommonFinancialStatementDto>> findFS_EntityCommonStockSharesOutstanding(@RequestParam(name = "cik") String cik) {

        CommonFinancialStatementDto response =
                abroadFinancialStatementService.findFS_EntityCommonStockSharesOutstanding(cik);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

}
