package com.finance.dart.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.finance.dart.common.constant.ResponseEnum;
import lombok.Data;

@Data
public class CommonResponse<T> {

    private boolean isSuccess;  // success
    private String code;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T response;


    public CommonResponse() {
        this.isSuccess = ResponseEnum.OK.isSuccess();
        this.code = ResponseEnum.OK.getCode();
        this.message = ResponseEnum.OK.getMessage();
    }

    @JsonIgnore
    public void setResponeInfo(ResponseEnum responseEnum) {
        this.isSuccess = responseEnum.isSuccess();
        this.code = responseEnum.getCode();
        this.message = responseEnum.getMessage();
    }

}
