package com.finance.dart.common.util;

public class StringUtil {

    public static String defaultString(Object value) {

        if(value == null) return "";
        if("".equals(value.toString().trim())) return "";

        return value.toString();
    }

}
