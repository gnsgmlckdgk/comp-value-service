package com.finance.dart.board.dto;

import lombok.Data;

import java.util.List;

/**
 * 자유게시판 목록 응답 DTO
 */
@Data
public class FreeBoardListResponseDto {

    private List<FreeBoardDto> notices; // 공지글 목록
    private List<FreeBoardDto> posts;   // 일반글 목록
    private long totalPosts;            // 일반글 전체 건수 (페이징용)
}


