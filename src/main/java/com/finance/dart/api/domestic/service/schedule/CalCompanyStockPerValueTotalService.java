package com.finance.dart.api.domestic.service.schedule;

import com.finance.dart.common.logging.TransactionLogging;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * <pre>
 * 가치계산 서비스 스케줄러 서비스
 * CalCompanyStockPerValueTotalService(스케줄러) >> CalCompanyStockPerValueTotalWorker(처리)
 * </pre>
 */
@Slf4j
@AllArgsConstructor
@Service
public class CalCompanyStockPerValueTotalService {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final CalCompanyStockPerValueTotalWorker calCompanyStockPerValueTotalWorker;


    @TransactionLogging
    @Scheduled(cron = "0 0 1 * * *") // 24시간 형식, 초 분 시 일 월 요일
    public void startScheduledTask() {
        executorService.submit(this::processDataInBackground);
    }

    private void processDataInBackground() {
        if(log.isDebugEnabled()) log.info("[가치계산 스케줄러] ExecutorService 시작");
        calCompanyStockPerValueTotalWorker.process();
    }

    @PreDestroy
    public void shutdownExecutor() {
        executorService.shutdown();
        if(log.isDebugEnabled()) log.info("[가치계산 스케줄러] ExecutorService 종료");
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                log.error("[가치계산 스케줄러] ExecutorService 강제 종료");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            log.error("[가치계산 스케줄러] ExecutorService 종료 오류");
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }



}
