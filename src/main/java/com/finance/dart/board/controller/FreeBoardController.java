package com.finance.dart.board.controller;

import com.finance.dart.board.entity.FreeBoard;
import com.finance.dart.board.service.FreeBoardService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@Slf4j
@AllArgsConstructor
@RequestMapping("freeboard")
@RestController
public class FreeBoardController {

    private final FreeBoardService freeBoardService;


    @GetMapping("")
    public ResponseEntity<Map<String, Object>> getFreeBoard(
            @RequestParam(name = "page",defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "sgubun", defaultValue = "0") String sgubun
    ) {

        Pageable pageable = PageRequest.of("".equals(page) ? 0 : page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Map<String, Object> response = freeBoardService.getAllBoards(pageable, search, sgubun);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<FreeBoard> viewFreeBoard(@PathVariable("id") Long id) {

        FreeBoard registedBoard = freeBoardService.getBoardById(id);

        return new ResponseEntity<>(registedBoard, HttpStatus.OK);
    }

    @PostMapping("/regi")
    public ResponseEntity<FreeBoard> regiFreeBoard(@RequestBody FreeBoard freeBoard) {

        FreeBoard registedBoard = freeBoardService.createBoard(freeBoard);

        return new ResponseEntity<>(registedBoard, HttpStatus.CREATED);
    }

    @PutMapping("/modi")
    public ResponseEntity<FreeBoard> modiFreeBoard(@RequestBody FreeBoard freeBoard) {

        FreeBoard updateBoard = freeBoardService.updateBoard(freeBoard.getId(), freeBoard);

        return new ResponseEntity<>(updateBoard, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Object> deleteFreeBoard(@PathVariable("id") Long id) {
        freeBoardService.deleteBoard(id);
        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
    }

}
