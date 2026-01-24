package com.finance.dart.cointrade.repository;

import com.finance.dart.cointrade.entity.CointradeConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 코인 자동매매 설정값 Repository
 */
public interface CointradeConfigRepository extends JpaRepository<CointradeConfigEntity, Long> {

    /**
     * 키 값으로 설정값 조회
     * @param paramName 설정 키
     * @return 설정값 Entity
     */
    Optional<CointradeConfigEntity> findByParamName(String paramName);
}