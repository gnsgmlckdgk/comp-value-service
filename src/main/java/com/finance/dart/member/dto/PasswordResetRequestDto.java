package com.finance.dart.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 비밀번호 재설정 요청 DTO
 */
@Data
public class PasswordResetRequestDto {

    @NotBlank(message = "사용자명을 입력해주세요.")
    private String username;

    @NotBlank(message = "이메일을 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
}
