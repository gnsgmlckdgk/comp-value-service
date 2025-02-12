package com.finance.dart.api.enums;

import lombok.Getter;

/**
 * 개별/연결 구분코드
 */
@Getter
public enum FsDiv {

    개별 ("OFS", "개별재무제표"),
    연결 ("CFS", "연결재무제표"),
    ;

    private final String code;
    private final String desc;

    FsDiv(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
