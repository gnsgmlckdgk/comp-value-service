package com.finance.dart.member.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "member", schema = "public")
public class MemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "username 을 입력해주세요.")
    @Column(nullable = false, unique = true)
    private String username;

    @JsonIgnore // 비밀번호는 절대 응답에 포함하지 않음
    @NotBlank(message = "password 을 입력해주세요.")
    @Column(nullable = false, unique = false)
    private String password;

    @Column(nullable = true, unique = false)
    private String email;

    @NotBlank(message = "nickname 을 입력해주세요.")
    @Column(nullable = false, unique = false)
    private String nickname;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();


    //@ OnToMany 설정으로 상대쪽 테이블에 외래키 컬럼만 존재
    // Cascade 설정으로 회원탈퇴시 아래 외래키로 참조하고 있는 테이블 데이터도 같이 삭제됨

    @JsonIgnore
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<MemberRoleEntity> memberRoles = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.finance.dart.board.entity.FreeBoard> freeBoards = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.finance.dart.board.entity.TranRecordEntity> tranRecords = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.finance.dart.board.entity.SellRecordEntity> sellRecords = new ArrayList<>();
}
