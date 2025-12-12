package com.finance.dart.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 관리자용 회원정보 수정 DTO
 */
@Data
public class AdminMemberUpdateDto {

    @NotNull(message = "회원 ID를 입력해주세요.")
    private Long memberId;

    private String email;

    private String nickname;
}
