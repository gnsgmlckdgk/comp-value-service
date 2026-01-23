package com.finance.dart.cointrade.controller;

import com.finance.dart.cointrade.dto.CointradeMlModelDto;
import com.finance.dart.cointrade.service.CointradeMlModelService;
import com.finance.dart.common.config.EndPointConfig;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.common.logging.TransactionLogging;
import com.finance.dart.member.enums.RoleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 코인 자동매매 ML 모델 정보 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/cointrade/ml-models")
@RequiredArgsConstructor
public class CointradeMlModelController {

    private final CointradeMlModelService cointradeMlModelService;

    /**
     * ML 모델 전체 목록 조회
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping
    public ResponseEntity<CommonResponse<List<CointradeMlModelDto>>> getAllModels() {
        log.info("ML 모델 전체 목록 조회 요청");
        List<CointradeMlModelDto> models = cointradeMlModelService.getAllModels();
        return new ResponseEntity<>(new CommonResponse<>(models), HttpStatus.OK);
    }
}