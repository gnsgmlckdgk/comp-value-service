package com.finance.dart.member.dto;

import lombok.Data;

/**
 * 회원 목록 조회 요청 DTO
 */
@Data
public class MemberListRequestDto {

    private Integer pageSize;           // (필수) 한번에 조회할 개수
    private Integer pageNumber = 0;     // 페이지 번호 (0부터 시작, 기본값: 0)

    private String username;            // 검색 - 유저아이디 (LIKE '%검색어%')
    private String email;               // 검색 - 이메일 (LIKE '%검색어%')
    private String nickname;            // 검색 - 닉네임 (LIKE '%검색어%')
    private String createdAtStart;      // 검색 - 가입일자 시작 (YYYY-MM-DD)
    private String createdAtEnd;        // 검색 - 가입일자 종료 (YYYY-MM-DD)

    private String approvalStatus;      // 회원등록승인여부 (Y/N, null이면 전체조회)
    private String roleName;            // 검색 - 권한명 (예: ROLE_ADMIN, ROLE_USER)
}
