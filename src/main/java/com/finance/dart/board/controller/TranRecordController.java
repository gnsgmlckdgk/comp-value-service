package com.finance.dart.board.controller;

import com.finance.dart.board.dto.*;
import com.finance.dart.board.entity.TranRecordEntity;
import com.finance.dart.board.service.TranRecordService;
import com.finance.dart.common.dto.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@AllArgsConstructor
@RequestMapping("tranrecord")
@RestController
public class TranRecordController {

    private final TranRecordService tranRecordService;


    /**
     * 거래기록 등록
     * @param reqBody
     * @return
     */
    @PostMapping("/regi")
    public ResponseEntity<CommonResponse<TranRecordDto>> regiTranRecord(HttpServletRequest request, @RequestBody TranRecordDto reqBody) {

        TranRecordDto result = tranRecordService.regiTranRecord(request, reqBody);

        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 거래기록 수정
     * @param request
     * @param reqBody
     * @return
     */
    @PostMapping("/modi")
    public ResponseEntity<CommonResponse<TranRecordDto>> modiTranRecord(HttpServletRequest request, @RequestBody TranRecordDto reqBody) {

        TranRecordDto result = tranRecordService.modiTranRecord(request, reqBody);

        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
    }

    /**
     * 거래기록 삭제
     * @param reqBody
     * @return
     */
    @PostMapping("/del")
    public ResponseEntity<CommonResponse<Void>> delTranRecord(@RequestBody TranRecordDto reqBody) {

        tranRecordService.delTranRecord(reqBody);

        return new ResponseEntity<>(new CommonResponse<>(), HttpStatus.OK);
    }


    /**
     * 거래기록 목록 조회
     * @param request
     * @return
     */
    @GetMapping("")
    public ResponseEntity<CommonResponse<List<TranRecordDto>>> getTranRecordList(HttpServletRequest request) {

        List<TranRecordDto> response = tranRecordService.getTranRecordList(request);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 티커 당 현재가격 조회
     * @param reqBody
     * @return
     */
    @PostMapping("/price")
    public ResponseEntity<CommonResponse<List<TranRecordCurValueResDto>>> getTranRecordCurValue(
            @RequestBody TranRecordCurValueReqDto reqBody
            ) {

        Set<String> symbols = reqBody.getSymbols().stream().collect(Collectors.toSet());

        List<TranRecordCurValueResDto> response = tranRecordService.getCurValues(symbols);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 환율정보 조회
     * @param reqBody
     * @return
     */
    @PostMapping("/rate")
    public ResponseEntity<CommonResponse<TranRecordFxRateResDto>> getCurrencyRate(
            @RequestBody TranRecordFxRateReqDto reqBody
    ) {

        TranRecordFxRateResDto response = tranRecordService.getFxRate(reqBody.getCurrency());

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

}
