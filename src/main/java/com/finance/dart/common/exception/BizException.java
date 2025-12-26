package com.finance.dart.common.exception;


import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 예외
 * - 200 응답으로 에러 메시지 전달
 */
@Getter
@ToString
public class BizException extends RuntimeException {

    private HttpStatus httpStatus;
    private String code;
    private String message;
    private CommonResponse body = null;


    public BizException(ResponseEnum responseEnum) {
        super(responseEnum.getMessage());
        this.httpStatus = responseEnum.getHttpStatus();
        this.code = responseEnum.getCode();
        this.message = responseEnum.getMessage();
    }

    public BizException(ResponseEnum responseEnum, CommonResponse body) {
        super(responseEnum.getMessage());
        this.httpStatus = responseEnum.getHttpStatus();
        this.code = responseEnum.getCode();
        this.message = responseEnum.getMessage();
        this.body = body;
    }

    public BizException(HttpStatus httpStatus, String code, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    public BizException(String code, String message) {
        super(message);
        this.httpStatus = ResponseEnum.INTERNAL_SERVER_ERROR.getHttpStatus();
        this.code = code;
        this.message = message;
    }

    /**
     * 메세지만 있는 경우 업무 오류로 판단
     * @param message
     */
    public BizException(String message) {
        super(message);
        this.httpStatus = ResponseEnum.BIZ_ERROR.getHttpStatus();
        this.code = ResponseEnum.BIZ_ERROR.getCode();
        this.message = message;
    }
}
