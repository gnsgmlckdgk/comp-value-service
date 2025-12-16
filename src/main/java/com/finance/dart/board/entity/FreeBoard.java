package com.finance.dart.board.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.finance.dart.member.entity.MemberEntity;
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

    @Column(columnDefinition = "text")
    private String content;

    @JsonIgnore // 순환 참조 방지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member; // 작성자 (Member 테이블 참조)

    @Deprecated // 더 이상 사용하지 않음 (member로 대체)
    private String author;

    private Integer viewCount = 0;

    @Column(name = "is_notice")
    private boolean notice = false;

    @Column(name = "is_secret")
    private boolean secret = false;


    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
