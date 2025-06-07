package com.finance.dart.common.exception;


/**
 * 인증오류 Exception
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}
