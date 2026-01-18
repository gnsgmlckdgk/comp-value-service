package com.finance.dart.cointrade.repository;

import com.finance.dart.cointrade.entity.CointradeHoldingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 코인 자동매매 보유 종목 Repository
 */
public interface CointradeHoldingRepository extends JpaRepository<CointradeHoldingEntity, Long> {

    /**
     * 코인 코드로 보유 종목 조회
     * @param coinCode 코인 코드
     * @return 보유 종목 Entity
     */
    Optional<CointradeHoldingEntity> findByCoinCode(String coinCode);

    /**
     * 코인 코드로 보유 종목 삭제
     * @param coinCode 코인 코드
     */
    void deleteByCoinCode(String coinCode);
}
