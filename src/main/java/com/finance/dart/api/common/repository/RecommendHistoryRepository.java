package com.finance.dart.api.common.repository;

import com.finance.dart.api.common.entity.RecommendHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 추천 종목 이력 Repository
 */
public interface RecommendHistoryRepository extends JpaRepository<RecommendHistoryEntity, Long> {

    /**
     * 특정 일자·프로파일의 이력 조회
     */
    List<RecommendHistoryEntity> findBySnapshotDateAndProfileName(LocalDate snapshotDate, String profileName);

    /**
     * 같은 날 재실행 시 기존 스냅샷 삭제 (멱등 저장용)
     */
    void deleteBySnapshotDateAndProfileName(LocalDate snapshotDate, String profileName);

    /**
     * 프로파일의 최신 스냅샷 1행 (최신 일자 판별용)
     */
    Optional<RecommendHistoryEntity> findFirstByProfileNameOrderBySnapshotDateDesc(String profileName);
}
