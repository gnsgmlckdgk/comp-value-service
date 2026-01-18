package com.finance.dart.cointrade.repository;

import com.finance.dart.cointrade.entity.CointradeConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 코인 자동매매 설정값 Repository
 */
public interface CointradeConfigRepository extends JpaRepository<CointradeConfigEntity, Long> {

    /**
     * 파라미터 이름으로 설정값 조회
     * @param paramName 파라미터 이름
     * @return 설정값 Entity
     */
    Optional<CointradeConfigEntity> findByParamName(String paramName);
}
