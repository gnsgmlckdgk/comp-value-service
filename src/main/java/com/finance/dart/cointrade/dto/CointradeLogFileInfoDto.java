package com.finance.dart.cointrade.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 코인 자동매매 로그 파일 정보 DTO
 */
@Getter
@Setter
@ToString
public class CointradeLogFileInfoDto {

    /** 파일명 */
    private String filename;

    /** 파일 크기 (bytes) */
    private Long size;

    /** 수정 시간 */
    @JsonProperty("modified_at")
    private String modifiedAt;

    /** 파일 경로 */
    private String path;
}
