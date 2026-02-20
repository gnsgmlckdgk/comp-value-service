package com.finance.dart.monitoring.service;

import com.finance.dart.cointrade.entity.CointradeTradeHistoryEntity;
import com.finance.dart.cointrade.repository.CointradeTradeHistoryRepository;
import com.finance.dart.monitoring.dto.TradeEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DB watermark 방식으로 신규 거래 감지
 * createdAt > lastChecked 쿼리로 새 거래를 탐지
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TradeEventDetectorService {

    private final CointradeTradeHistoryRepository tradeHistoryRepository;

    private volatile LocalDateTime lastChecked = LocalDateTime.now();

    /**
     * 마지막 체크 이후 발생한 신규 거래 조회
     */
    public List<TradeEventDto> detectNewTrades() {
        LocalDateTime checkpoint = lastChecked;
        List<CointradeTradeHistoryEntity> newTrades =
                tradeHistoryRepository.findByCreatedAtAfterOrderByCreatedAtAsc(checkpoint);

        if (!newTrades.isEmpty()) {
            // watermark 갱신: 가장 최근 거래의 createdAt
            lastChecked = newTrades.get(newTrades.size() - 1).getCreatedAt();
            log.debug("신규 거래 {}건 감지 (watermark: {} → {})", newTrades.size(), checkpoint, lastChecked);
        }

        return newTrades.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 오늘 총 거래 건수
     */
    public int getTodayTradeCount() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return tradeHistoryRepository.findByCreatedAtBetween(startOfDay, endOfDay).size();
    }

    private TradeEventDto toDto(CointradeTradeHistoryEntity entity) {
        return TradeEventDto.builder()
                .id(entity.getId())
                .coinCode(entity.getCoinCode())
                .tradeType(entity.getTradeType())
                .price(entity.getPrice())
                .quantity(entity.getQuantity())
                .totalAmount(entity.getTotalAmount())
                .profitLoss(entity.getProfitLoss())
                .profitLossRate(entity.getProfitLossRate())
                .reason(entity.getReason())
                .timestamp(entity.getCreatedAt())
                .build();
    }
}
