package com.finance.dart.cointrade.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 코인 자동매매 로그 파일 목록 DTO
 */
@Getter
@Setter
@ToString
public class CointradeLogFileListDto {

    /** 로그 파일 목록 */
    private List<CointradeLogFileInfoDto> files;
}
