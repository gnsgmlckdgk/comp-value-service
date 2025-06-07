package com.finance.dart.common.constant;

import lombok.Getter;

@Getter
public enum ResponseEnum {

    OK("20000", "정상 처리되었습니다.", true),

    LOGIN_NOTFOUND_USER("40400", "user가 존재하지 않습니다.", false),
    LOGIN_NOTMATCH_PASSWORD("40000", "비밀번호가 일치하지 않습니다.", false),

    JOIN_DUPLICATE_USERNAME("40000", "이미 등록된 username 입니다.", false),
    ;


    private String code;
    private String message;
    private boolean isSuccess;

    ResponseEnum(String code, String message, boolean isSuccess) {
        this.code = code;
        this.message = message;
        this.isSuccess = isSuccess;
    }
}
