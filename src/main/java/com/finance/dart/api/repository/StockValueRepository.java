package com.finance.dart.api.repository;

import com.finance.dart.api.entity.StockValuationResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StockValueRepository extends JpaRepository<StockValuationResultEntity, Long> {
    long deleteByBaseDateLessThanEqual(String baseDate);
}

