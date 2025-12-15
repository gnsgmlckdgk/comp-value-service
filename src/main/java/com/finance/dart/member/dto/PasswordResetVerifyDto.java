package com.finance.dart.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 비밀번호 재설정 인증 DTO
 */
@Data
public class PasswordResetVerifyDto {

    @NotBlank(message = "사용자명을 입력해주세요.")
    private String username;

    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;

    @NotBlank(message = "인증코드를 입력해주세요.")
    @Pattern(regexp = "^[0-9]{6}$", message = "인증코드는 6자리 숫자입니다.")
    private String verificationCode;
}
