package com.finance.dart.board.dto;

import lombok.Data;

import java.util.List;

/**
 * 자유게시판 목록 응답 DTO
 */
@Data
public class FreeBoardListResponseDto {

    private List<FreeBoardDto> data; // 게시글 목록
    private long total;              // 전체 건수
}


