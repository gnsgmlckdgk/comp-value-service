package com.finance.dart.api.enums;

import lombok.Getter;

/**
 * 거래소 코드
 */
@Getter
public enum ExchangeCd {

    코스피 ("KS", ".KS", "KSE", "코스피"),
    코스닥 ("KQ", ".KQ", "KOSDAQ", "코스닥"),
    ;

    private final String code;
    private final String urlCode;
    private final String fullExchangeName;
    private final String desc;

    ExchangeCd(String code, String urlCode, String fullExchangeName, String desc) {
        this.code = code;
        this.urlCode = urlCode;
        this.fullExchangeName = fullExchangeName;
        this.desc = desc;
    }

}
