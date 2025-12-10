package com.finance.dart.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MemberJoinReqDto {

    @NotBlank(message = "username 을 입력해주세요.")
    private String username;

    @NotBlank(message = "password 을 입력해주세요.")
    private String password;

    private String email;

    @NotBlank(message = "nickname 을 입력해주세요.")
    private String nickname;

}
