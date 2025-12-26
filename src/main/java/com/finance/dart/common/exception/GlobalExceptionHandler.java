package com.finance.dart.common.exception;

import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse> handleException(Exception e) {

        CommonResponse response = new CommonResponse(ResponseEnum.INTERNAL_SERVER_ERROR);
        response.setResponse(e.getMessage());

        return ResponseEntity
                .status(response.getHttpStatus())
                .body(response);
    }


    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<CommonResponse> handleUnauthorized(UnauthorizedException ex) {

        CommonResponse response = new CommonResponse(ResponseEnum.LOGIN_SESSION_EXPIRED);

        return ResponseEntity
                .status(response.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(BizException.class)
    public ResponseEntity<CommonResponse> handleBizException(BizException ex) {

        CommonResponse response = new CommonResponse(
                false, ex.getCode(), ex.getMessage(), ex.getHttpStatus(), ex.getBody()
        );

        return ResponseEntity
                .status(ex.getHttpStatus())
                .body(response);
    }
}
