package com.finance.dart.board.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.finance.dart.member.entity.MemberEntity;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "freeboard_comment", schema = "public")
public class FreeBoardComment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "freeboard_comment_seq")
    @SequenceGenerator(
            name = "freeboard_comment_seq",
            sequenceName = "freeboard_comment_seq",
            allocationSize = 1
    )
    private Long id;

    @Column(columnDefinition = "text", nullable = false)
    private String content;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freeboard_id", nullable = false)
    private FreeBoard freeBoard;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private MemberEntity member;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private FreeBoardComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FreeBoardComment> replies = new ArrayList<>();

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(nullable = false)
    private int depth = 0;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
