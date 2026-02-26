package com.finance.dart.cointrade.controller;

import com.finance.dart.cointrade.dto.*;
import com.finance.dart.cointrade.service.CointradeBacktestService;
import com.finance.dart.common.config.EndPointConfig;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.common.logging.TransactionLogging;
import com.finance.dart.member.enums.RoleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 백테스트 관리 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/backtest")
@RequiredArgsConstructor
public class CointradeBacktestController {

    private final CointradeBacktestService backtestService;

    /**
     * 백테스트 실행
     * POST /api/backtest/run
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @PostMapping("/run")
    public ResponseEntity<CommonResponse<BacktestRunResDto>> runBacktest(
            @RequestBody BacktestRunReqDto request) {
        log.info("백테스트 실행 요청: {}", request);
        BacktestRunResDto result = backtestService.runBacktest(request);
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 백테스트 작업 상태 조회
     * GET /api/backtest/status/{taskId}
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/status/{taskId}")
    public ResponseEntity<CommonResponse<BacktestStatusDto>> getStatus(
            @PathVariable(name = "taskId") String taskId) {
        log.info("백테스트 작업 상태 조회 요청: {}", taskId);
        BacktestStatusDto status = backtestService.getStatus(taskId);
        return new ResponseEntity<>(new CommonResponse<>(status), HttpStatus.OK);
    }

    /**
     * 백테스트 결과 조회
     * GET /api/backtest/result/{taskId}
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/result/{taskId}")
    public ResponseEntity<CommonResponse<BacktestResultDto>> getResult(
            @PathVariable(name = "taskId") String taskId,
            @RequestParam(required = false, name = "include_individual") Boolean includeIndividual,
            @RequestParam(required = false, name = "include_trades") Boolean includeTrades) {
        log.info("백테스트 결과 조회 요청 - taskId: {}, includeIndividual: {}, includeTrades: {}",
                taskId, includeIndividual, includeTrades);
        BacktestResultDto result = backtestService.getResult(taskId, includeIndividual, includeTrades);
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 백테스트 결과 삭제
     * DELETE /api/backtest/result/{taskId}
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @DeleteMapping("/result/{taskId}")
    public ResponseEntity<CommonResponse<BacktestDeleteResDto>> deleteResult(
            @PathVariable(name = "taskId") String taskId) {
        log.info("백테스트 결과 삭제 요청: {}", taskId);
        BacktestDeleteResDto result = backtestService.deleteResult(taskId);
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 백테스트 이력 조회
     * GET /api/backtest/history
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/history")
    public ResponseEntity<CommonResponse<List<BacktestHistoryDto>>> getHistory() {
        log.info("백테스트 이력 조회 요청");
        List<BacktestHistoryDto> history = backtestService.getHistory();
        return new ResponseEntity<>(new CommonResponse<>(history), HttpStatus.OK);
    }

    /**
     * 백테스트 작업 취소
     * POST /api/backtest/cancel/{taskId}
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @PostMapping("/cancel/{taskId}")
    public ResponseEntity<CommonResponse<BacktestDeleteResDto>> cancelBacktest(
            @PathVariable(name = "taskId") String taskId) {
        log.info("백테스트 작업 취소 요청: {}", taskId);
        BacktestDeleteResDto result = backtestService.cancelBacktest(taskId);
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 백테스트 제목 수정
     * PATCH /api/backtest/result/{taskId}
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @PatchMapping("/result/{taskId}")
    public ResponseEntity<CommonResponse<BacktestUpdateTitleResDto>> updateBacktestResult(
            @PathVariable(name = "taskId") String taskId,
            @RequestBody BacktestUpdateTitleReqDto request) {
        log.info("백테스트 제목 수정 요청 - taskId: {}, title: {}", taskId, request.getTitle());
        BacktestUpdateTitleResDto result = backtestService.updateBacktestTitle(taskId, request);
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 백테스트 옵티마이저 실행
     * POST /api/backtest/optimizer
     * 백테스트를 이용해 최적의 파라미터 값을 분석
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @PostMapping("/optimizer")
    public ResponseEntity<CommonResponse<OptimizerRunResDto>> runOptimizer(
            @RequestBody OptimizerRunReqDto request) {
        log.info("백테스트 옵티마이저 실행 요청: {}", request);
        OptimizerRunResDto result = backtestService.runOptimizer(request);
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 백테스트 옵티마이저 상태 조회
     * GET /api/backtest/optimizer/status/{taskId}
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/optimizer/status/{taskId}")
    public ResponseEntity<CommonResponse<OptimizerStatusDto>> getOptimizerStatus(
            @PathVariable(name = "taskId") String taskId) {
        log.info("백테스트 옵티마이저 상태 조회 요청: {}", taskId);
        OptimizerStatusDto status = backtestService.getOptimizerStatus(taskId);
        return new ResponseEntity<>(new CommonResponse<>(status), HttpStatus.OK);
    }

    /**
     * 백테스트 옵티마이저 결과 조회
     * GET /api/backtest/optimizer/result/{taskId}
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/optimizer/result/{taskId}")
    public ResponseEntity<CommonResponse<OptimizerResultDto>> getOptimizerResult(
            @PathVariable(name = "taskId") String taskId,
            @RequestParam(required = false, name = "include_all_trials") Boolean includeAllTrials) {
        log.info("백테스트 옵티마이저 결과 조회 요청 - taskId: {}, includeAllTrials: {}", taskId, includeAllTrials);
        OptimizerResultDto result = backtestService.getOptimizerResult(taskId, includeAllTrials);
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 백테스트 옵티마이저 이력 조회
     * GET /api/backtest/optimizer/history
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/optimizer/history")
    public ResponseEntity<CommonResponse<List<OptimizerHistoryDto>>> getOptimizerHistory(
            @RequestParam(required = false, name = "limit") Integer limit) {
        log.info("백테스트 옵티마이저 이력 조회 요청 - limit: {}", limit);
        List<OptimizerHistoryDto> history = backtestService.getOptimizerHistory(limit);
        return new ResponseEntity<>(new CommonResponse<>(history), HttpStatus.OK);
    }

    /**
     * 백테스트 옵티마이저 작업 취소
     * POST /api/backtest/optimizer/cancel/{taskId}
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @PostMapping("/optimizer/cancel/{taskId}")
    public ResponseEntity<CommonResponse<BacktestDeleteResDto>> cancelOptimizer(
            @PathVariable(name = "taskId") String taskId) {
        log.info("백테스트 옵티마이저 작업 취소 요청: {}", taskId);
        BacktestDeleteResDto result = backtestService.cancelOptimizer(taskId);
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 백테스트 옵티마이저 제목 수정
     * PATCH /api/backtest/optimizer/result/{taskId}
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @PatchMapping("/optimizer/result/{taskId}")
    public ResponseEntity<CommonResponse<OptimizerUpdateTitleResDto>> updateOptimizerResult(
            @PathVariable(name = "taskId") String taskId,
            @RequestBody OptimizerUpdateTitleReqDto request) {
        log.info("백테스트 옵티마이저 제목 수정 요청 - taskId: {}, title: {}", taskId, request.getTitle());
        OptimizerUpdateTitleResDto result = backtestService.updateOptimizerTitle(taskId, request);
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 백테스트 옵티마이저 결과 삭제
     * DELETE /api/backtest/optimizer/result/{taskId}
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @DeleteMapping("/optimizer/result/{taskId}")
    public ResponseEntity<CommonResponse<BacktestDeleteResDto>> deleteOptimizerResult(
            @PathVariable(name = "taskId") String taskId) {
        log.info("백테스트 옵티마이저 결과 삭제 요청: {}", taskId);
        BacktestDeleteResDto result = backtestService.deleteOptimizerResult(taskId);
        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }
}
