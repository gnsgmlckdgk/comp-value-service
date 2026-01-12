package com.finance.dart.api.common.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class MgntRedisResDto {
    private boolean isSuccess = false;
    private String message = "";
}
