package com.finance.dart.common.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseEnum {

    OK("20000", "정상 처리되었습니다.", true, HttpStatus.OK),

    LOGIN_NOTFOUND_USER("40400", "user가 존재하지 않습니다.", false, HttpStatus.BAD_REQUEST),
    LOGIN_NOTMATCH_PASSWORD("40000", "비밀번호가 일치하지 않습니다.", false, HttpStatus.BAD_REQUEST),
    LOGIN_SESSION_EXPIRED("40100", "인증정보가 존재하지않습니다.", false, HttpStatus.UNAUTHORIZED),

    JOIN_DUPLICATE_USERNAME("40000", "이미 등록된 username 입니다.", false, HttpStatus.BAD_REQUEST),
    ;


    private String code;
    private String message;
    private boolean isSuccess;
    private HttpStatus httpStatus;


    ResponseEnum(String code, String message, boolean isSuccess, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.isSuccess = isSuccess;
        this.httpStatus = httpStatus;
    }
}
