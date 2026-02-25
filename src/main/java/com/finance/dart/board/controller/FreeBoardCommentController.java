package com.finance.dart.board.controller;

import com.finance.dart.board.dto.FreeBoardCommentDto;
import com.finance.dart.board.service.FreeBoardCommentService;
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
@RequestMapping("freeboard")
@RestController
public class FreeBoardCommentController {

    private final FreeBoardCommentService commentService;

    @TransactionLogging
    @GetMapping("/{boardId}/comments")
    public ResponseEntity<CommonResponse<List<FreeBoardCommentDto>>> getComments(
            @PathVariable("boardId") Long boardId) {

        List<FreeBoardCommentDto> comments = commentService.getComments(boardId);
        return new ResponseEntity<>(new CommonResponse<>(comments), HttpStatus.OK);
    }

    @TransactionLogging
    @PostMapping("/{boardId}/comments")
    public ResponseEntity<CommonResponse<FreeBoardCommentDto>> createComment(
            HttpServletRequest request,
            @PathVariable("boardId") Long boardId,
            @RequestBody FreeBoardCommentDto dto) {

        FreeBoardCommentDto created = commentService.createComment(request, boardId, dto);
        return new ResponseEntity<>(new CommonResponse<>(created), HttpStatus.CREATED);
    }

    @TransactionLogging
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommonResponse<FreeBoardCommentDto>> updateComment(
            HttpServletRequest request,
            @PathVariable("commentId") Long commentId,
            @RequestBody FreeBoardCommentDto dto) {

        FreeBoardCommentDto updated = commentService.updateComment(request, commentId, dto);
        return new ResponseEntity<>(new CommonResponse<>(updated), HttpStatus.OK);
    }

    @TransactionLogging
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<CommonResponse<Void>> deleteComment(
            HttpServletRequest request,
            @PathVariable("commentId") Long commentId) {

        commentService.deleteComment(request, commentId);
        return new ResponseEntity<>(new CommonResponse<>(), HttpStatus.OK);
    }
}
