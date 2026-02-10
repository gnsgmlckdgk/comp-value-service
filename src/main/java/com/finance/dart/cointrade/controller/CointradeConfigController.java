package com.finance.dart.cointrade.controller;

import com.finance.dart.cointrade.dto.*;
import com.finance.dart.cointrade.service.CointradeConfigService;
import com.finance.dart.common.config.EndPointConfig;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.common.logging.TransactionLogging;
import com.finance.dart.member.enums.RoleConstants;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 코인 자동매매 설정 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/cointrade")
@RequiredArgsConstructor
public class CointradeConfigController {

    private final CointradeConfigService cointradeConfigService;

    /**
     * 1. 전체 설정값 조회
     * GET /api/cointrade/config
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/config")
    public ResponseEntity<CommonResponse<List<CointradeConfigDto>>> getAllConfigs() {
        log.info("전체 설정값 조회 요청");
        List<CointradeConfigDto> configs = cointradeConfigService.getAllConfigs();
        return new ResponseEntity<>(new CommonResponse<>(configs), HttpStatus.OK);
    }

    /**
     * 2. 설정값 일괄 수정
     * PUT /api/cointrade/config
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @PutMapping("/config")
    public ResponseEntity<CommonResponse<String>> updateConfigs(
            @RequestBody List<CointradeConfigUpdateDto> configs) {
        log.info("설정값 일괄 수정 요청: {}개", configs.size());
        cointradeConfigService.updateConfigs(configs);
        return new ResponseEntity<>(new CommonResponse<>("설정값 수정 완료"), HttpStatus.OK);
    }

    /**
     * 3. 대상 종목 목록 조회 (DB 조회)
     * GET /api/cointrade/coins
     */
    @TransactionLogging
    @GetMapping("/coins")
    public ResponseEntity<CommonResponse<List<CointradeTargetCoinDto>>> getAllTargetCoins() {
        log.info("대상 종목 목록 조회 요청");
        List<CointradeTargetCoinDto> coins = cointradeConfigService.getAllTargetCoins();
        return new ResponseEntity<>(new CommonResponse<>(coins), HttpStatus.OK);
    }

    /**
     * 4. 대상 종목 설정
     * PUT /api/cointrade/coins
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @PutMapping("/coins")
    public ResponseEntity<CommonResponse<String>> updateTargetCoins(
            @RequestBody List<String> coinCodes) {
        log.info("대상 종목 설정 요청: {}", coinCodes);
        cointradeConfigService.updateTargetCoins(coinCodes);
        return new ResponseEntity<>(new CommonResponse<>("대상 종목 설정 완료"), HttpStatus.OK);
    }

    /**
     * 5. 보유 종목 조회
     * GET /api/cointrade/holdings
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/holdings")
    public ResponseEntity<CommonResponse<List<CointradeHoldingDto>>> getAllHoldings() {
        log.info("보유 종목 조회 요청");
        List<CointradeHoldingDto> holdings = cointradeConfigService.getAllHoldings();
        return new ResponseEntity<>(new CommonResponse<>(holdings), HttpStatus.OK);
    }

    /**
     * 6. 거래 기록 조회 (페이징)
     * GET /api/cointrade/history
     * Query Params: startDate, endDate, coinCode, tradeType, page, size
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/history")
    public ResponseEntity<CommonResponse<Page<CointradeTradeHistoryDto>>> getTradeHistory(
            @RequestParam(required = false, name = "startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false, name = "endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false, name = "coinCode") String coinCode,
            @RequestParam(required = false, name = "tradeType") String tradeType,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("거래 기록 조회 요청 - startDate: {}, endDate: {}, coinCode: {}, tradeType: {}, page: {}",
                startDate, endDate, coinCode, tradeType, pageable.getPageNumber());

        Page<CointradeTradeHistoryDto> history = cointradeConfigService.getTradeHistory(
                startDate, endDate, coinCode, tradeType, pageable);

        return new ResponseEntity<>(new CommonResponse<>(history), HttpStatus.OK);
    }

    /**
     * 7. 매수 스케줄러 ON/OFF
     * PUT /api/cointrade/scheduler/buy
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @PutMapping("/scheduler/buy")
    public ResponseEntity<CommonResponse<String>> updateBuyScheduler(
            @RequestBody CointradeSchedulerReqDto request) {
        log.info("매수 스케줄러 설정 변경 요청: {}", request.getEnabled());
        cointradeConfigService.updateBuyScheduler(request.getEnabled());
        return new ResponseEntity<>(
                new CommonResponse<>("매수 스케줄러 설정 완료: " + request.getEnabled()),
                HttpStatus.OK);
    }

    /**
     * 8. 매도 스케줄러 ON/OFF
     * PUT /api/cointrade/scheduler/sell
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @PutMapping("/scheduler/sell")
    public ResponseEntity<CommonResponse<String>> updateSellScheduler(
            @RequestBody CointradeSchedulerReqDto request) {
        log.info("매도 스케줄러 설정 변경 요청: {}", request.getEnabled());
        cointradeConfigService.updateSellScheduler(request.getEnabled());
        return new ResponseEntity<>(
                new CommonResponse<>("매도 스케줄러 설정 완료: " + request.getEnabled()),
                HttpStatus.OK);
    }

    /**
     * 9. 현재 상태 조회
     * GET /api/cointrade/status
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/status")
    public ResponseEntity<CommonResponse<CointradeStatusDto>> getStatus() {
        log.info("현재 상태 조회 요청");
        CointradeStatusDto status = cointradeConfigService.getStatus();
        return new ResponseEntity<>(new CommonResponse<>(status), HttpStatus.OK);
    }

    /**
     * 10. 매수 프로세스 수동 실행
     * GET /api/cointrade/trade/buy/start
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/trade/buy/start")
    public ResponseEntity<CommonResponse<Map<String, Object>>> startBuyProcess() {
        log.info("매수 프로세스 수동 실행 요청");
        Map<String, Object> result = cointradeConfigService.startBuyProcess();
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 11. 매도 프로세스 수동 실행
     * GET /api/cointrade/trade/sell/start
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/trade/sell/start")
    public ResponseEntity<CommonResponse<Map<String, Object>>> startSellProcess() {
        log.info("매도 프로세스 수동 실행 요청");
        Map<String, Object> result = cointradeConfigService.startSellProcess();
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 12. 매수/매도 프로세스 수동 중지
     * @return
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/trade/stop")
    public ResponseEntity<CommonResponse<Map<String, Object>>> stopProcess() {
        log.info("매수/매도 프로세스 수동 중지 요청");
        Map<String, Object> result = cointradeConfigService.stopProcess();
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 13. 모델 수동 학습
     * @return
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/trade/model/train")
    public ResponseEntity<CommonResponse<Map<String, Object>>> modelTrain(
            @RequestParam(name = "coin_code") @Nullable String coinCode) {
        log.info("모델 학습 수동 실행 요청");
        Map<String, Object> result = cointradeConfigService.modelTrain(coinCode);
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 14. 스케줄러 설정 즉시 리로드
     * POST /api/cointrade/scheduler/reload
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @PostMapping("/scheduler/reload")
    public ResponseEntity<CommonResponse<Map<String, Object>>> reloadScheduler() {
        log.info("스케줄러 설정 즉시 리로드 요청");
        Map<String, Object> result = cointradeConfigService.reloadScheduler();
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 15. 업비트 KRW 잔액 조회
     * GET /api/cointrade/account/balance
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/account/balance")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getAccountBalance() {
        log.info("업비트 KRW 잔액 조회 요청");
        Map<String, Object> result = cointradeConfigService.getAccountBalance();
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

}