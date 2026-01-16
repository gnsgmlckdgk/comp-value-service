package com.finance.dart.api.common.repository;

import com.finance.dart.api.common.entity.StockPredictionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface StockPredictionRepository extends JpaRepository<StockPredictionEntity, Long> {

    /**
     * 티커와 예측 날짜로 예측 데이터 조회
     *
     * @param ticker         티커 심볼
     * @param predictionDate 예측 날짜
     * @return 예측 데이터
     */
    Optional<StockPredictionEntity> findByTickerAndPredictionDate(String ticker, LocalDate predictionDate);
}
