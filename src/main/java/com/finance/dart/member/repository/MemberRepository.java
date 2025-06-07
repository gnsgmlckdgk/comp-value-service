package com.finance.dart.board.repository;

import com.finance.dart.board.entity.FreeBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FreeBoardRepository extends JpaRepository<FreeBoard, Long> {
    // 필요한 커스텀 메서드 추가 가능

    /**
     * 검색어 추가 목록 조회
     * @param title 제목
     * @param content 내용
     * @param pageable
     * @return
     */
    Page<FreeBoard> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);

    /**
     * 검색어 추가 목록 조회
     * @param content 내용
     * @param pageable
     * @return
     */
    Page<FreeBoard> findByContentContaining(String content, Pageable pageable);

    /**
     * 검색어 추가 목록 조회
     * @param title 제목
     * @param pageable
     * @return
     */
    Page<FreeBoard> findByTitleContaining(String title, Pageable pageable);

    /**
     * 검색어 추가 목록 조회
     * @param author 작성자
     * @param pageable
     * @return
     */
    Page<FreeBoard> findByAuthorContaining(String author, Pageable pageable);

}

