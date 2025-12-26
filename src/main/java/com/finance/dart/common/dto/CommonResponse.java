package com.finance.dart.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.finance.dart.common.constant.ResponseEnum;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@JsonPropertyOrder({ "success", "code", "message", "response" })
public class CommonResponse<T> {

    @JsonProperty("success")
    private boolean isSuccess;  // success
    private String code;
    private String message;

    @JsonIgnore
    private HttpStatus httpStatus;

//    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T response;


    /**
     * 정상 응답 기본값
     */
    public CommonResponse() {
        this.isSuccess = ResponseEnum.OK.isSuccess();
        this.code = ResponseEnum.OK.getCode();
        this.message = ResponseEnum.OK.getMessage();
        this.httpStatus = ResponseEnum.OK.getHttpStatus();
    }

    /**
     * 정상 응답 기본값
     * @param response 응답 바디
     */
    public CommonResponse(T response) {
        this.isSuccess = ResponseEnum.OK.isSuccess();
        this.code = ResponseEnum.OK.getCode();
        this.message = ResponseEnum.OK.getMessage();
        this.httpStatus = ResponseEnum.OK.getHttpStatus();
        this.response = response;
    }

    public CommonResponse(boolean isSuccess, String code, String message, HttpStatus httpStatus, T response) {
        this.isSuccess = isSuccess;
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
        this.response = response;
    }


    public CommonResponse(ResponseEnum responseEnum) {
        setResponeInfo(responseEnum);
    }

    public CommonResponse(ResponseEnum responseEnum, T response) {
        setResponeInfo(responseEnum);
        this.response = response;
    }

    @JsonIgnore
    public void setResponeInfo(ResponseEnum responseEnum) {
        this.isSuccess = responseEnum.isSuccess();
        this.code = responseEnum.getCode();
        this.message = responseEnum.getMessage();
        this.httpStatus = responseEnum.getHttpStatus();
    }

}
