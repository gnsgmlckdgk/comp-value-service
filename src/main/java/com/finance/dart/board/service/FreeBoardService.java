package com.finance.dart.board.service;

import com.finance.dart.board.entity.FreeBoard;
import com.finance.dart.board.repository.FreeBoardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
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
    public List<FreeBoard> getAllBoards(Pageable pageable) {
        Page<FreeBoard> pageFreeBoard = freeBoardRepository.findAll(pageable);
        return pageFreeBoard.stream().toList();
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
