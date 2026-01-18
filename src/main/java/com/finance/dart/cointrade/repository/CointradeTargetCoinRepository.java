package com.finance.dart.cointrade.repository;

import com.finance.dart.cointrade.entity.CointradeTargetCoinEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 코인 자동매매 대상 종목 Repository
 */
public interface CointradeTargetCoinRepository extends JpaRepository<CointradeTargetCoinEntity, Long> {

    /**
     * 코인 코드로 대상 종목 조회
     * @param coinCode 코인 코드
     * @return 대상 종목 Entity
     */
    Optional<CointradeTargetCoinEntity> findByCoinCode(String coinCode);

    /**
     * 활성화 여부로 대상 종목 목록 조회
     * @param isActive 활성화 여부
     * @return 대상 종목 Entity 목록
     */
    List<CointradeTargetCoinEntity> findByIsActive(Boolean isActive);
}
