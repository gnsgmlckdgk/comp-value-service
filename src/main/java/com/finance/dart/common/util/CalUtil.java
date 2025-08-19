package com.finance.dart.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;

/**
 * 계산 유틸
 */
public class CalUtil {

    /**
     * 더하기
     * @param val1
     * @param val2
     * @return
     */
    public static String add(String val1, String val2) {
        BigDecimal num1 = new BigDecimal(deleteComma(val1));
        BigDecimal num2 = new BigDecimal(deleteComma(val2));

        BigDecimal addition = num1.add(num2);
        return addition.toPlainString();
    }

    /**
     * 빼기
     * @param val1
     * @param val2
     * @return
     */
    public static String sub(String val1, String val2) {
        BigDecimal num1 = new BigDecimal(deleteComma(val1));
        BigDecimal num2 = new BigDecimal(deleteComma(val2));

        BigDecimal subtraction = num1.subtract(num2);
        return subtraction.toPlainString();
    }

    /**
     * 곱하기
     * @param val1
     * @param val2
     * @return
     */
    public static String multi(String val1, String val2) {
        BigDecimal num1 = new BigDecimal(deleteComma(val1));
        BigDecimal num2 = new BigDecimal(deleteComma(val2));

        BigDecimal multiplication = num1.multiply(num2);
        return multiplication.toPlainString();
    }

    /**
     * 나누기
     * @param val1
     * @param val2
     * @param roundScale
     * @param roundingMode
     * @return
     */
    public static String divide(String val1, String val2, int roundScale, RoundingMode roundingMode) {
        BigDecimal num1 = new BigDecimal(deleteComma(val1));
        BigDecimal num2 = new BigDecimal(deleteComma(val2));

        BigDecimal division = num1.divide(num2, roundScale, roundingMode);
        return division.toPlainString();
    }

    public static String divide(String val1, String val2, RoundingMode roundingMode) {

        if("".equals(val1)) val1 = "0";
        if("".equals(val2)) val2 = "0";

        BigDecimal num1 = new BigDecimal(deleteComma(val1));
        BigDecimal num2 = new BigDecimal(deleteComma(val2));

        if (num2.compareTo(BigDecimal.ZERO) == 0) {
            return "0";
        }

        BigDecimal division = num1.divide(num2, 0, roundingMode);
        return division.toPlainString();
    }

    /**
     * 비교
     * @param val1
     * @param val2
     * @return 1: num1>num2 / -1: num2>num1 / 0: num1==num2
     */
    public static int compare(String val1, String val2) {
        BigDecimal num1 = new BigDecimal(deleteComma(val1));
        BigDecimal num2 = new BigDecimal(deleteComma(val2));

        int comparison = num1.compareTo(num2);
        return comparison;
    }

    /**
     * 소수점 크기 변환
     * @param val
     * @param roundScale
     * @param roundingMode
     * @return
     */
    public static String scale(String val, int roundScale, RoundingMode roundingMode) {
        BigDecimal num1 = new BigDecimal(deleteComma(val));
        BigDecimal result = num1.setScale(roundScale, roundingMode);
        return result.toPlainString();
    }

    /**
     * 콤마 삭제
     * 123,789,987 => 123789987
     * @param val
     * @return
     */
    private static String deleteComma(String val) {
        if("".equals(val)) return "";
        NumberFormat format = NumberFormat.getInstance();
        try {
            Number number = format.parse(val);
            return number.toString();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

}
