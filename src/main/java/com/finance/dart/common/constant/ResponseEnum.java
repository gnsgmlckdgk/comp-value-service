package com.finance.dart.common.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ResponseEnum {

    // 정상
    OK("20000", "정상 처리되었습니다.", true, HttpStatus.OK),

    // 공통 오류
    INTERNAL_SERVER_ERROR("50000", "서버처리중 오류가 발생했습니다.", false, HttpStatus.INTERNAL_SERVER_ERROR),

    // 로그인 관련 오류
    LOGIN_NOTFOUND_USER("40400", "user가 존재하지 않습니다.", false, HttpStatus.BAD_REQUEST),
    LOGIN_NOTMATCH_PASSWORD("40000", "비밀번호가 일치하지 않습니다.", false, HttpStatus.BAD_REQUEST),
    LOGIN_SESSION_EXPIRED("40100", "인증정보가 존재하지않습니다.", false, HttpStatus.UNAUTHORIZED),

    // 회원가입 관련 오류
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
