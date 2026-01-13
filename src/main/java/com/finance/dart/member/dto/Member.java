package com.finance.dart.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Member {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String createdAt;
    private String updatedAt;
    private List<String> roles;

    private Long sessionTTL;    // 로그인세션남은시간
}
