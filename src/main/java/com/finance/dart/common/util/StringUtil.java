package com.finance.dart.common.util;

public class StringUtil {

    public static String defaultString(Object value) {

        if(value == null) return "";
        if("".equals(value.toString().trim())) return "";

        return value.toString();
    }


    public static boolean isStringEmpty(Object value) {
        if(value == null) return true;
        if("".equals(value.toString().trim())) return true;

        return false;
    }

}
