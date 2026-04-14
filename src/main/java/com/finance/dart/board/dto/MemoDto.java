package com.finance.dart.board.dto;

import lombok.Data;

/**
 * 개인 메모 DTO
 */
@Data
public class MemoDto {

    private Long id;
    private String title;
    private String content;
    private String category;
    private boolean pinned;
    private String createdAt;
    private String updatedAt;
}
