package com.finance.dart.board.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "freeboard", schema = "public")
public class FreeBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "freeboard_seq")
    @SequenceGenerator(
            name = "freeboard_seq",          // 시퀀스 생성기 이름
            sequenceName = "freeboard_seq",  // 데이터베이스에 생성될 시퀀스 이름
            allocationSize = 1               // 한 번에 증가하는 값 (필요에 따라 조정 가능)
    )
    private Long id;

    private String title;

    @Lob
    @Column(columnDefinition = "text")
    private String content;

    private String author;

    private Integer viewCount = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
