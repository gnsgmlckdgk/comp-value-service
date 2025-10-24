package com.finance.dart.board.controller;

import com.finance.dart.board.dto.TranRecordDto;
import com.finance.dart.board.entity.TranRecordEntity;
import com.finance.dart.board.service.TranRecordService;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.member.dto.Member;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@AllArgsConstructor
@RequestMapping("tranrecord")
@RestController
public class TranRecordController {

    private final TranRecordService tranRecordService;


    // TODO: 거래기록 수정, 삭제 추가 예정

    /**
     * 거래기록 등록
     * @param reqBody
     * @return
     */
    @PostMapping("/regi")
    public ResponseEntity<CommonResponse<TranRecordEntity>> regiTranRecord(HttpServletRequest request, @RequestBody TranRecordEntity reqBody) {

        TranRecordEntity result = tranRecordService.regiTranRecord(request, reqBody);

        return new ResponseEntity<>(new CommonResponse<>(result), HttpStatus.OK);
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

}
