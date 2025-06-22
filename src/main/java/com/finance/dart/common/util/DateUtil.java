package com.finance.dart.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static String getToday() {
        return DateUtil.getToday("yyyyMMdd HH:mm:ss");
    }

    public static String getToday(String pattern) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }


    /**
     * <pre>
     * 날짜 구하기
     * </pre>
     * @param daysOffset +(미래날짜), -(과거날짜), 0(오늘날짜)
     * @param pattern yyyyMMdd
     * @return
     */
    public static String getOffsetDate(int daysOffset, String pattern) {

        if(StringUtil.isStringEmpty(pattern)) pattern = "yyyyMMdd";

        return LocalDate.now().plusDays(daysOffset)
                .format(DateTimeFormatter.ofPattern(pattern));
    }

}
