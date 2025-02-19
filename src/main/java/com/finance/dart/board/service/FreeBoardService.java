package com.finance.dart.board.service;

import com.finance.dart.board.entity.FreeBoard;
import com.finance.dart.board.repository.FreeBoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FreeBoardService {

    @Autowired
    private FreeBoardRepository freeBoardRepository;

    // Create: 새 게시글 생성
    public FreeBoard createBoard(FreeBoard board) {
        // 생성 시 기본 생성일, 수정일을 설정할 수 있음
        board.setCreatedAt(LocalDateTime.now());
        board.setUpdatedAt(LocalDateTime.now());
        return freeBoardRepository.save(board);
    }

    // Read: ID로 게시글 조회
    public FreeBoard getBoardById(Long id) {
        // Optional을 반환하므로, 값이 없을 경우 예외 처리 가능
        Optional<FreeBoard> boardOpt = freeBoardRepository.findById(id);
        return boardOpt.orElseThrow(() ->
                new RuntimeException("Board not found with id: " + id));
    }

    // Read: 모든 게시글 조회(페이징)
    // Pageable pageable = PageRequest.of(page, size);
    @Transactional(readOnly = true)
    public Map<String, Object> getAllBoards(Pageable pageable, String search, String sgubun) {

        Page<FreeBoard> page = null;

        // 검색 값 투입
        if("1".equals(sgubun)) {          // 제목으로 검색
            page = freeBoardRepository.findByTitleContaining(search, pageable);
        } else if("2".equals(sgubun)) {   // 작성자로 검색
            page = freeBoardRepository.findByAuthorContaining(search, pageable);
        } else if("3".equals(sgubun)) {   // 내용으로 검색
            page = freeBoardRepository.findByContentContaining(search, pageable);
        } else if("4".equals(sgubun)) {   // 제목, 내용으로 검색
            page = freeBoardRepository.findByTitleContainingOrContentContaining(search, search, pageable);
        } else {    // 전체 조회
            page = freeBoardRepository.findAll(pageable);
        }

        List<FreeBoard> boards = page.getContent();
        long totalElements = page.getTotalElements();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("data", boards);
        response.put("total", totalElements);

        return response;
    }

    // Update: 게시글 수정
    public FreeBoard updateBoard(Long id, FreeBoard boardDetails) {
        FreeBoard board = getBoardById(id);  // 기존 게시글을 조회
        board.setTitle(boardDetails.getTitle());
        board.setContent(boardDetails.getContent());
        board.setAuthor(boardDetails.getAuthor());
        board.setUpdatedAt(LocalDateTime.now());  // 수정일 갱신
        return freeBoardRepository.save(board);
    }

    // Delete: 게시글 삭제
    public void deleteBoard(Long id) {
        // 게시글을 조회한 후 삭제
        FreeBoard board = getBoardById(id);
        freeBoardRepository.delete(board);
    }


}
