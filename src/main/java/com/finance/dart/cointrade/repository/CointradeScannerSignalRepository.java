package com.finance.dart.cointrade.repository;

import com.finance.dart.cointrade.entity.CointradeScannerSignalEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 코인 자동매매 스캐너 시그널 Repository
 */
public interface CointradeScannerSignalRepository extends JpaRepository<CointradeScannerSignalEntity, Long> {

    /**
     * 코인 코드로 시그널 조회
     * @param coinCode 코인 코드
     * @return 시그널 목록
     */
    List<CointradeScannerSignalEntity> findByCoinCode(String coinCode);

    /**
     * 특정 시간 이후 시그널 조회
     * @param detectedAt 기준 시간
     * @return 시그널 목록
     */
    List<CointradeScannerSignalEntity> findByDetectedAtAfterOrderByDetectedAtDesc(LocalDateTime detectedAt);
}
