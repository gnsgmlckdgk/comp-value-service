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
     * 현재 시간을 나노초 단위로 반환
     * @return 나노초 timestamp
     */
    public static long getCurrentNanoTime() {
        return System.nanoTime();
    }

    /**
     * 두 시간 사이의 경과 시간을 밀리초로 계산
     * @param startNanoTime 시작 시간 (나노초)
     * @param endNanoTime 종료 시간 (나노초)
     * @return 경과 시간 (밀리초)
     */
    public static long getElapsedTimeMillis(long startNanoTime, long endNanoTime) {
        return (endNanoTime - startNanoTime) / 1_000_000;
    }

    /**
     * 시작 시간부터 현재까지의 경과 시간을 밀리초로 계산
     * @param startNanoTime 시작 시간 (나노초)
     * @return 경과 시간 (밀리초)
     */
    public static long getElapsedTimeMillis(long startNanoTime) {
        return getElapsedTimeMillis(startNanoTime, System.nanoTime());
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

    /**
     * 날짜 비교 (yyyy-MM-dd)
     * @param dateStr1 A
     * @param dateStr2 B
     * @return -1(A가 B이전), 0(같은 날짜), 1(A가 B이후)
     */
    public static int compareDate(String dateStr1, String dateStr2) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date1 = LocalDate.parse(dateStr1, formatter);
        LocalDate date2 = LocalDate.parse(dateStr2, formatter);

        if (date1.isBefore(date2)) {
            return -1;
        } else if (date1.isAfter(date2)) {
            return 1;
        } else {
            return 0;
        }
    }

    /**
     * LocalDate to String
     * @param localDate
     * @param pattern null(defalut: yyyy-MM-dd)
     * @return
     */
    public static String convertLocaleDateToString(LocalDate localDate, String pattern) {
        if(localDate == null) return "";
        if(StringUtil.isStringEmpty(pattern)) pattern = "yyyy-MM-dd";
        return StringUtil.defaultString(localDate.format(DateTimeFormatter.ofPattern(pattern)));
    }
}
