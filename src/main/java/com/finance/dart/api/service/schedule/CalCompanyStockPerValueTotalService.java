package com.finance.dart.api.service.schedule;

import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@AllArgsConstructor
@Service
public class CalCompanyStockPerValueTotalService {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final CalCompanyStockPerValueTotalWorker calCompanyStockPerValueTotalWorker;


    @Scheduled(cron = "0 0 9 * * *") // 24시간 형식, 초 분 시 일 월 요일 TODO: 시간대 새벽으로 변경 예정
    public void startScheduledTask() {
        executorService.submit(this::processDataInBackground);
    }

    private void processDataInBackground() {
        calCompanyStockPerValueTotalWorker.process();
    }

    @PreDestroy
    public void shutdownExecutor() {
        executorService.shutdown();
        if(log.isDebugEnabled()) log.debug("[가치계산 스케줄러] ExecutorService 종료");
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                if(log.isDebugEnabled()) log.debug("[가치계산 스케줄러] ExecutorService 강제 종료");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            if(log.isDebugEnabled()) log.debug("[가치계산 스케줄러] ExecutorService 종료 오류");
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }



}
