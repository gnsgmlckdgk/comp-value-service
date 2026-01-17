package com.finance.dart.api.abroad.dto.ml;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 로그 파일 목록 DTO
 */
@Getter
@Setter
@ToString
public class LogFileListDto {

    /** 로그 파일 목록 */
    private List<LogFileInfoDto> files;
}
