package com.finance.dart.board.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "freeboard_attachment", schema = "public")
public class FreeBoardAttachment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "freeboard_attachment_seq")
    @SequenceGenerator(
            name = "freeboard_attachment_seq",
            sequenceName = "freeboard_attachment_seq",
            allocationSize = 1
    )
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freeboard_id", nullable = false)
    private FreeBoard freeBoard;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "stored_filename", nullable = false)
    private String storedFilename;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
