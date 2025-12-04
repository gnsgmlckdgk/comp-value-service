package com.finance.dart.board.dto;

import lombok.Data;

/**
 * 자유게시판 DTO
 */
@Data
public class FreeBoardDto {

    private Long id;                // 게시글 ID
    private String title;           // 제목
    private String content;         // 내용
    private Integer viewCount;      // 조회수

    // 작성자 정보
    private Long memberId;          // 작성자 ID (프론트에서 본인 글 확인용)
    private String memberUsername;  // 작성자 username
    private String memberNickname;  // 작성자 닉네임 (화면 표시용)

    private String createdAt;       // 작성일
    private String updatedAt;       // 수정일
}
