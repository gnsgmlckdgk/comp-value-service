package com.finance.dart.api.abroad.dto.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * 필수 파라미터 DTO
 */
@Getter
@RequiredArgsConstructor
public class RequireParamDto {
    /**
     * API KEY
     */
    private final String apikey;
}
