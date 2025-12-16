package com.finance.dart.member.enums;

/**
 * 역할 상수 클래스
 * - 어노테이션에서 사용할 수 있는 컴파일 타임 상수 정의
 */
public class RoleConstants {

    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    private RoleConstants() {
        // 인스턴스 생성 방지
    }
}
