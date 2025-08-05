package com.finance.dart.api.common.repository;

import com.finance.dart.api.common.entity.StockValuationResultEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface StockValueRepository extends JpaRepository<StockValuationResultEntity, Long> {
    long deleteByBaseDateLessThanEqual(String baseDate);
}

