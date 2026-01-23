package com.finance.dart.common.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Component
public class TransactionLoggingAspect {

    private static final String MDC_KEY = "transaction_log_file";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Around("@annotation(transactionLogging)")
    public Object logTransaction(ProceedingJoinPoint joinPoint, TransactionLogging transactionLogging) throws Throwable {
        String name = transactionLogging.value();
        if (name.isEmpty()) {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String className = signature.getDeclaringType().getSimpleName();
            String methodName = signature.getMethod().getName();
            name = className + "_" + methodName;
        }

        String date = LocalDateTime.now().format(formatter);
        String logFileName = String.format("%s_%s", name, date);

        try {
            MDC.put(MDC_KEY, logFileName);
            return joinPoint.proceed();
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
