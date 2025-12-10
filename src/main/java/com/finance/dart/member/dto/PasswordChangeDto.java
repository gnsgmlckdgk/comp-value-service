package com.finance.dart.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 비밀번호 변경 DTO
 */
@Data
public class PasswordChangeDto {

    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;

    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    private String newPassword;
}
