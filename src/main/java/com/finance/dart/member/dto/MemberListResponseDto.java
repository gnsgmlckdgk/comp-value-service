package com.finance.dart.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * 회원 목록 조회 응답 DTO
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberListResponseDto {

    private Long totalCount;            // 전체 회원 수
    private Integer pageSize;           // 페이지 크기
    private Integer pageNumber;         // 현재 페이지 번호
    private Integer totalPages;         // 전체 페이지 수

    private List<MemberDto> members;    // 회원 목록

    /**
     * 회원 정보 DTO (권한 정보 포함)
     */
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MemberDto {
        private Long id;
        private String username;
        private String email;
        private String nickname;
        private String approvalStatus;      // 승인 상태 (Y/N)
        private String createdAt;
        private String updatedAt;
        private List<String> roles;         // 권한 목록
    }
}
