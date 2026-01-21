package com.finance.dart.common.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 개별 거래별 또는 스케줄러별 로그 생성을 위한 어노테이션
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TransactionLogging {
    String value() default ""; // 로그파일명에 사용될 이름 (미지정시 메소드명 사용)
}
