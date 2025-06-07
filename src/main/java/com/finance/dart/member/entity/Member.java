package com.finance.dart.member.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Entity
@Table(name = "member", schema = "public")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "username 을 입력해주세요.")
    @Column(nullable = false, unique = true)
    private String username;

    @NotBlank(message = "password 을 입력해주세요.")
    @Column(nullable = false)
    private String password;

    @Column(nullable = true, unique = false)
    private String email;

    @NotBlank(message = "nickname 을 입력해주세요.")
    @Column(nullable = false, unique = false)
    private String nickname;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
