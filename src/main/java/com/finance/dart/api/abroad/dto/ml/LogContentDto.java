package com.finance.dart.api.abroad.dto.ml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 로그 내용 DTO
 */
@Getter
@Setter
@ToString
public class LogContentDto {

    /** 파일명 */
    private String filename;

    /** 로그 내용 */
    private String content;

    /** 줄 수 */
    private Integer lines;

    /** 반환된 첫 줄 번호 */
    private Integer startLine;

    /** 반환된 마지막 줄 번호 (다음 요청시 lastLine으로 사용) */
    private Integer endLine;

    /** 전체 줄 수 */
    private Integer totalLines;

    /** 추가 로그 존재 여부 */
    private Boolean hasMore;
}
