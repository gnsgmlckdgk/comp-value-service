package com.finance.dart.cointrade.controller;

import com.finance.dart.cointrade.dto.CointradeSellRequestDto;
import com.finance.dart.cointrade.dto.CointradeSellResponseDto;
import com.finance.dart.cointrade.service.CointradeConfigService;
import com.finance.dart.common.config.EndPointConfig;
import com.finance.dart.common.logging.TransactionLogging;
import com.finance.dart.member.enums.RoleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 코인 자동매매 보유 종목 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/holdings")
@RequiredArgsConstructor
public class CointradeHoldingController {

    private final CointradeConfigService cointradeConfigService;

    /**
     * 보유 종목 매도
     * POST /api/holdings/sell
     *
     * @param request 매도 요청 (coin_codes가 없으면 전체 매도)
     * @return 매도 결과
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @PostMapping("/sell")
    public ResponseEntity<CointradeSellResponseDto> sellHoldings(
            @RequestBody(required = false) CointradeSellRequestDto request) {

        // request가 null인 경우 빈 객체 생성 (전체 매도)
        if (request == null) {
            request = new CointradeSellRequestDto();
        }

        log.info("보유 종목 매도 요청 - coinCodes: {}", request.getCoinCodes());

        CointradeSellResponseDto response = cointradeConfigService.sellHoldings(request);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
