package com.finance.dart.board.dto;

import lombok.Data;

import java.util.List;

@Data
public class FreeBoardCommentDto {

    private Long id;
    private String content;
    private Long freeBoardId;
    private Long parentId;
    private int depth;

    private Long memberId;
    private String memberUsername;
    private String memberNickname;

    private boolean deleted;

    private String createdAt;
    private String updatedAt;

    private List<FreeBoardCommentDto> replies;
}
