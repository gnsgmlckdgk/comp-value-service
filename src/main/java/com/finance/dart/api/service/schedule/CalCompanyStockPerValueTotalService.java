package com.finance.dart.api.service.schedule;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CalCompanyStockPerValueTotalService {

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

//    @Scheduled(cron = "0 0 X * * *") // 24시간 형식, 초 분 시 일 월 요일
    @Scheduled
    public void startScheduledTask() {
        executorService.submit(this::processDataInBackground);
    }

    private void processDataInBackground() {

        List dataList = new LinkedList();
        // TODO: 테스트
        dataList.add(Map.of("k", "v1"));
        dataList.add(Map.of("k", "v2"));
        dataList.add(Map.of("k", "v3"));


        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> data = (Map) dataList.get(i);
            try {
                // 실제 처리 로직
                if(log.isDebugEnabled()) log.debug("실제 로직 추가!! [{}]", data.get("k"));

                // 2초 대기
                Thread.sleep(2000);

            } catch (InterruptedException e) {
                log.error("Interrupted during sleep", e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("Error processing data: {}", data, e);
            }
        }
    }

    @PreDestroy
    public void shutdownExecutor() {
        executorService.shutdown();
        if(log.isDebugEnabled()) log.debug("ExecutorService 종료");
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                if(log.isDebugEnabled()) log.debug("ExecutorService 강제 종료");
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            if(log.isDebugEnabled()) log.debug("ExecutorService 종료 오류");
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
