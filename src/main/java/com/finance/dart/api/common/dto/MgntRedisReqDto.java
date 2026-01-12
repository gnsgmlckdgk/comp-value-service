package com.finance.dart.api.common.dto;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MgntRedisReqDto {

    private String key;     // 풀네임이거나 패턴
    private String value;
    private Long ttl;
    private String type;    // I: 등록, S: 조회, D: 삭제, PS: 패턴조회 ,PD: 패턴삭제

}
