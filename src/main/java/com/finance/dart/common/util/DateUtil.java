package com.finance.dart.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    public static String getToday() {
        return DateUtil.getToday("yyyyMMdd HH:mm:ss");
    }

    public static String getToday(String pattern) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
    }

}
