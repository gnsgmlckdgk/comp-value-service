package com.finance.dart.api.domestic.enums;

import lombok.Getter;

/**
 * 보고서 코드
 */
@Getter
public enum ReprtCode {

    분기보고서_1 ("11013", ""),
    반기보고서   ("11012", ""),
    분기보고서_3 ("11014", ""),
    사업보고서   ("11011", "")
    ;

    private final String code;
    private final String desc;

    ReprtCode(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

}
