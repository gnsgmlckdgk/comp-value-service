package com.finance.dart.common.exception;

import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<CommonResponse> handleUnauthorized(UnauthorizedException ex) {

        CommonResponse response = new CommonResponse(ResponseEnum.LOGIN_SESSION_EXPIRED);

        return ResponseEntity
                .status(ResponseEnum.LOGIN_SESSION_EXPIRED.getHttpStatus())
                .body(response);
    }
}
