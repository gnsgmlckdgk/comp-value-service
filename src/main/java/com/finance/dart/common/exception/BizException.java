package com.finance.dart.common.exception;


/**
 * 비즈니스 로직 예외
 * - 200 응답으로 에러 메시지 전달
 */
public class BizException extends RuntimeException {
    public BizException(String message) {
        super(message);
    }
}
