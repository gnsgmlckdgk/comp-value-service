package com.finance.dart.member.dto;

import lombok.Data;

/**
 * 비밀번호 재설정 응답 DTO
 */
@Data
public class PasswordResetResponseDto {

    private String username;
    private String temporaryPassword;
}
