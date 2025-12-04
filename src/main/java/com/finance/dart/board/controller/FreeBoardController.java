package com.finance.dart.board.controller;

import com.finance.dart.board.dto.FreeBoardDto;
import com.finance.dart.board.dto.FreeBoardListResponseDto;
import com.finance.dart.board.service.FreeBoardService;
import com.finance.dart.common.dto.CommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@AllArgsConstructor
@RequestMapping("freeboard")
@RestController
public class FreeBoardController {

    private final FreeBoardService freeBoardService;


    @GetMapping("")
    public ResponseEntity<CommonResponse<FreeBoardListResponseDto>> getFreeBoard(
            @RequestParam(name = "page",defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "sgubun", defaultValue = "0") String sgubun
    ) {

        Pageable pageable = PageRequest.of("".equals(page) ? 0 : page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        FreeBoardListResponseDto response = freeBoardService.getAllBoards(pageable, search, sgubun);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<CommonResponse<FreeBoardDto>> viewFreeBoard(@PathVariable("id") Long id) {

        FreeBoardDto board = freeBoardService.getBoardById(id);

        return new ResponseEntity<>(new CommonResponse<>(board), HttpStatus.OK);
    }

    @PostMapping("/regi")
    public ResponseEntity<CommonResponse<FreeBoardDto>> regiFreeBoard(
            HttpServletRequest request,
            @RequestBody FreeBoardDto freeBoard) {

        FreeBoardDto registedBoard = freeBoardService.createBoard(request, freeBoard);

        return new ResponseEntity<>(new CommonResponse<>(registedBoard), HttpStatus.CREATED);
    }

    @PutMapping("/modi")
    public ResponseEntity<CommonResponse<FreeBoardDto>> modiFreeBoard(
            HttpServletRequest request,
            @RequestBody FreeBoardDto freeBoard) {

        FreeBoardDto updateBoard = freeBoardService.updateBoard(request, freeBoard.getId(), freeBoard);

        return new ResponseEntity<>(new CommonResponse<>(updateBoard), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteFreeBoard(
            HttpServletRequest request,
            @PathVariable("id") Long id) {
        freeBoardService.deleteBoard(request, id);
        return new ResponseEntity<>(new CommonResponse<>(), HttpStatus.NO_CONTENT);
    }

}
