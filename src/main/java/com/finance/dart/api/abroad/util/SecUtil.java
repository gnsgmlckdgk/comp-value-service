package com.finance.dart.api.abroad.util;

/**
 * SEC API 유틸
 */
public class SecUtil {

    private final static String CONV_CIK_ID = "{cik}";

    /**
     * URL에 CIK 세팅
     * @param url
     * @param cik
     * @return
     */
    public static String setUrlCik(String url, String cik) {
        return url.replace(CONV_CIK_ID, cik);
    }

}
