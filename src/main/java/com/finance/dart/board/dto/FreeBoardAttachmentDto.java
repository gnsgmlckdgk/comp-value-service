package com.finance.dart.board.dto;

import lombok.Data;

@Data
public class FreeBoardAttachmentDto {

    private Long id;
    private String originalFilename;
    private Long fileSize;
    private String contentType;
    private String createdAt;
}
