package com.finance.dart.board.controller;

import com.finance.dart.board.dto.MemoDto;
import com.finance.dart.board.service.MemoService;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.common.logging.TransactionLogging;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@AllArgsConstructor
@RequestMapping("memo")
@RestController
public class MemoController {

    private final MemoService memoService;

    /**
     * 메모 목록 조회
     */
    @TransactionLogging
    @GetMapping("")
    public ResponseEntity<CommonResponse<List<MemoDto>>> getMemoList(
            HttpServletRequest request,
            @RequestParam(required = false) String category) {

        List<MemoDto> response = memoService.getMemoList(request, category);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 메모 단건 조회
     */
    @TransactionLogging
    @GetMapping("/{id}")
    public ResponseEntity<CommonResponse<MemoDto>> getMemo(
            HttpServletRequest request,
            @PathVariable Long id) {

        MemoDto response = memoService.getMemo(request, id);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 메모 생성
     */
    @TransactionLogging
    @PostMapping("/regi")
    public ResponseEntity<CommonResponse<MemoDto>> createMemo(
            HttpServletRequest request,
            @RequestBody MemoDto reqBody) {

        MemoDto response = memoService.createMemo(request, reqBody);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 메모 수정
     */
    @TransactionLogging
    @PutMapping("/modi")
    public ResponseEntity<CommonResponse<MemoDto>> updateMemo(
            HttpServletRequest request,
            @RequestBody MemoDto reqBody) {

        MemoDto response = memoService.updateMemo(request, reqBody);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 메모 삭제
     */
    @TransactionLogging
    @DeleteMapping("/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteMemo(
            HttpServletRequest request,
            @PathVariable Long id) {

        memoService.deleteMemo(request, id);

        return new ResponseEntity<>(new CommonResponse<>(), HttpStatus.OK);
    }

    /**
     * 카테고리 목록 조회
     */
    @TransactionLogging
    @GetMapping("/categories")
    public ResponseEntity<CommonResponse<List<String>>> getCategories(HttpServletRequest request) {

        List<String> response = memoService.getCategories(request);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }
}
