package com.finance.dart.board.controller;

import com.finance.dart.board.entity.FreeBoard;
import com.finance.dart.board.service.FreeBoardService;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Slf4j
@AllArgsConstructor
@RequestMapping("freeboard")
@RestController
public class FreeBoardController {

    private final FreeBoardService freeBoardService;


    @GetMapping("")
    public ResponseEntity<List<FreeBoard>> getFreeBoard(
            @RequestParam(name = "page",defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {

        Pageable pageable = PageRequest.of(page, size);
        List<FreeBoard> freeBoardList = freeBoardService.getAllBoards(pageable);

        return new ResponseEntity<>(freeBoardList, HttpStatus.OK);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<FreeBoard> viewFreeBoard(@PathVariable("id") Long id) {

        FreeBoard registedBoard = freeBoardService.getBoardById(id);

        return new ResponseEntity<>(registedBoard, HttpStatus.OK);
    }

    @PostMapping("/regi")
    public ResponseEntity<FreeBoard> regiFreeBoard(@RequestBody FreeBoard freeBoard) {

        FreeBoard registedBoard = freeBoardService.createBoard(freeBoard);

        return new ResponseEntity<>(registedBoard, HttpStatus.OK);
    }

}
