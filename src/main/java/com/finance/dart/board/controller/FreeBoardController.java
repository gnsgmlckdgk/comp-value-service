package com.finance.dart.board.controller;

import com.finance.dart.board.dto.FreeBoardDto;
import com.finance.dart.board.dto.FreeBoardListResponseDto;
import com.finance.dart.board.service.FreeBoardService;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.common.logging.TransactionLogging;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Slf4j
@AllArgsConstructor
@RequestMapping("freeboard")
@RestController
public class FreeBoardController {

    private final FreeBoardService freeBoardService;


    @TransactionLogging
    @GetMapping("")
    public ResponseEntity<CommonResponse<FreeBoardListResponseDto>> getFreeBoard(
            HttpServletRequest request,
            @RequestParam(name = "page",defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "sgubun", defaultValue = "0") String sgubun
    ) {

        Pageable pageable = PageRequest.of("".equals(page) ? 0 : page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        FreeBoardListResponseDto response = freeBoardService.getAllBoards(request, pageable, search, sgubun);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    @TransactionLogging
    @GetMapping("/view/{id}")
    public ResponseEntity<CommonResponse<FreeBoardDto>> viewFreeBoard(
            HttpServletRequest request,
            @PathVariable("id") Long id) {

        FreeBoardDto board = freeBoardService.getBoardById(request, id);

        return new ResponseEntity<>(new CommonResponse<>(board), HttpStatus.OK);
    }

    @TransactionLogging
    @PostMapping(value = "/regi", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<FreeBoardDto>> regiFreeBoard(
            HttpServletRequest request,
            @RequestPart("board") FreeBoardDto freeBoard,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {

        FreeBoardDto registedBoard = freeBoardService.createBoard(request, freeBoard, files);

        return new ResponseEntity<>(new CommonResponse<>(registedBoard), HttpStatus.CREATED);
    }

    @TransactionLogging
    @PutMapping(value = "/modi", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<FreeBoardDto>> modiFreeBoard(
            HttpServletRequest request,
            @RequestPart("board") FreeBoardDto freeBoard,
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @RequestPart(value = "deleteAttachmentIds", required = false) List<Long> deleteAttachmentIds) {

        FreeBoardDto updateBoard = freeBoardService.updateBoard(request, freeBoard.getId(), freeBoard, files, deleteAttachmentIds);

        return new ResponseEntity<>(new CommonResponse<>(updateBoard), HttpStatus.OK);
    }

    @TransactionLogging
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<CommonResponse<Void>> deleteFreeBoard(
            HttpServletRequest request,
            @PathVariable("id") Long id) {
        freeBoardService.deleteBoard(request, id);
        return new ResponseEntity<>(new CommonResponse<>(), HttpStatus.NO_CONTENT);
    }

}