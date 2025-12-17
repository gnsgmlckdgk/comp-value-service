package com.finance.dart.board.repository;

import com.finance.dart.board.entity.FreeBoard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface FreeBoardRepository extends JpaRepository<FreeBoard, Long> {
    // 필요한 커스텀 메서드 추가 가능

    /**
     * 공지글 전체 조회 (페이징 없음)
     */
    List<FreeBoard> findByNoticeTrue(Sort sort);

    /**
     * 일반글 조회 (notice = false)
     */
    Page<FreeBoard> findByNoticeFalse(Pageable pageable);

    /**
     * 검색어 추가 목록 조회 (일반글만)
     * @param title 제목
     * @param content 내용
     * @param pageable
     * @return
     */
    Page<FreeBoard> findByNoticeFalseAndTitleContainingOrNoticeFalseAndContentContaining(String title, String content, Pageable pageable);

    /**
     * 검색어 추가 목록 조회 (일반글만)
     * @param content 내용
     * @param pageable
     * @return
     */
    Page<FreeBoard> findByNoticeFalseAndContentContaining(String content, Pageable pageable);

    /**
     * 검색어 추가 목록 조회 (일반글만)
     * @param title 제목
     * @param pageable
     * @return
     */
    Page<FreeBoard> findByNoticeFalseAndTitleContaining(String title, Pageable pageable);

    /**
     * 작성자 닉네임으로 검색 (member.nickname 사용, 일반글만)
     * @param nickname 작성자 닉네임
     * @param pageable
     * @return
     */
    Page<FreeBoard> findByNoticeFalseAndMember_NicknameContaining(String nickname, Pageable pageable);

    /**
     * 작성자 아이디로 검색 (member.username 사용, 일반글만)
     * @param username 작성자 아이디
     * @param pageable
     * @return
     */
    Page<FreeBoard> findByNoticeFalseAndMember_UsernameContaining(String username, Pageable pageable);

}

