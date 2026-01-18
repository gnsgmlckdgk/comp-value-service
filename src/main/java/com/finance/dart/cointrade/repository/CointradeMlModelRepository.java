package com.finance.dart.cointrade.repository;

import com.finance.dart.cointrade.entity.CointradeMlModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 코인 자동매매 ML 모델 정보 Repository
 */
public interface CointradeMlModelRepository extends JpaRepository<CointradeMlModelEntity, Long> {

    /**
     * 코인 코드로 ML 모델 조회
     * @param coinCode 코인 코드
     * @return ML 모델 Entity
     */
    Optional<CointradeMlModelEntity> findByCoinCode(String coinCode);
}
