package com.finance.dart.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 회원 탈퇴 DTO
 */
@Data
public class MemberDeleteDto {

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password;
}
