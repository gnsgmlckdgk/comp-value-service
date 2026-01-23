package com.finance.dart.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Aspect
@Component
public class TransactionLoggingAspect {

    private static final String MDC_KEY = "transaction_log_file";
    private static final DateTimeFormatter fileFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter logTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Around("@annotation(transactionLogging)")
    public Object logTransaction(ProceedingJoinPoint joinPoint, TransactionLogging transactionLogging) throws Throwable {
        String name = transactionLogging.value();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getMethod().getName();

        if (name.isEmpty()) {
            name = className + "_" + methodName;
        }

        String date = LocalDateTime.now().format(fileFormatter);
        String logFileName = String.format("%s_%s", name, date);

        try {
            MDC.put(MDC_KEY, logFileName);
            
            String startTime = LocalDateTime.now().format(logTimeFormatter);
            log.info("[{}] - [{}] / [{}] 요청", className, methodName, startTime);

            Object result = joinPoint.proceed();

            String endTime = LocalDateTime.now().format(logTimeFormatter);
            log.info("[{}] - [{}] / [{}] 응답", className, methodName, endTime);

            return result;
        } finally {
            MDC.remove(MDC_KEY);
        }
    }
}
