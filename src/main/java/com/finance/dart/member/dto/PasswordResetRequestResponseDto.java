package com.finance.dart.member.dto;

import lombok.Data;

/**
 * 비밀번호 재설정 요청 응답 DTO
 */
@Data
public class PasswordResetRequestResponseDto {

    private String email;
    private Long expiresInSeconds;
    private String expiresAt;
}
