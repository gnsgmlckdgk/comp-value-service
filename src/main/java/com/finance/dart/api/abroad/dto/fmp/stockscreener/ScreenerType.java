package com.finance.dart.api.abroad.dto.fmp.stockscreener;

public enum ScreenerType {

    // conservative (보수적)
    CONSERVATIVE(1),

    // aggressive (공격적)
    AGGRESSIVE(2),

    ;

    private final int code;

    ScreenerType(int code) {
        this.code = code;
    }

}
