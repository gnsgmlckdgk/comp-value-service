package com.finance.dart.member.enums;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum Role {

    SUPER_ADMIN("ROLE_SUPER_ADMIN", "슈퍼 관리자"),
    ADMIN("ROLE_ADMIN", "관리자"),
    USER("ROLE_USER", "일반 유저")
    ;


    private String roleName;
    private String desc;

    Role(String roleName, String desc) {
        this.roleName = roleName;
        this.desc = desc;
    }
}
