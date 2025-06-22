package com.finance.dart.common.util;

public class StringUtil {

    public static String defaultString(Object value) {

        if(value == null) return "";
        if("".equals(value.toString().trim())) return "";

        return value.toString();
    }

    public static long defaultLong(Object value) {

        if (value == null) return 0;

        try {
            if (value instanceof Number) {
                return ((Number) value).longValue();  // Integer, Long, Double 모두 포함
            } else {
                return Long.parseLong(value.toString().trim());
            }
        } catch (NumberFormatException e) {
            return 0;  // 파싱 실패 시 기본값 반환
        }

    }

    public static boolean isStringEmpty(Object value) {
        if(value == null) return true;
        if("".equals(value.toString().trim())) return true;

        return false;
    }


    /**
     * 지정된 문자열을 주어진 문자셋 기준으로 최대 byteLength 바이트까지 자른다.
     * 멀티바이트 문자가 중간에 잘리지 않도록 안전하게 처리함.
     *
     * @param value       자를 문자열
     * @param byteLength  잘라낼 바이트 수 (예: 20)
     * @param charset     사용할 문자셋 (예: "UTF-8", "MS949")
     * @return 바이트 기준 최대 byteLength 바이트까지 잘린 문자열
     */
    public static String cutStringDelete(String value, long byteLength, String charset) {
        if (value == null || charset == null || byteLength <= 0) return "";

        try {
            byte[] bytes = value.getBytes(charset);
            if (bytes.length <= byteLength) return value;

            int byteCount = 0;
            StringBuilder result = new StringBuilder();

            for (char ch : value.toCharArray()) {
                byte[] chBytes = String.valueOf(ch).getBytes(charset);
                if (byteCount + chBytes.length > byteLength) break;
                byteCount += chBytes.length;
                result.append(ch);
            }

            return result.toString();
        } catch (Exception e) {
            return ""; // 인코딩 오류 발생 시 빈 문자열 반환
        }
    }
}
