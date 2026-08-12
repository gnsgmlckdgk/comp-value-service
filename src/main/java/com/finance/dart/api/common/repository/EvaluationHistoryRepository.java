package com.finance.dart.api.common.repository;

import com.finance.dart.api.common.entity.EvaluationHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 종목 평가 이력 Repository
 */
public interface EvaluationHistoryRepository extends JpaRepository<EvaluationHistoryEntity, Long> {

    /**
     * 특정 일자의 평가 이력 조회
     */
    List<EvaluationHistoryEntity> findBySnapshotDate(LocalDate snapshotDate);

    /**
     * 최신 평가 일자 1행
     */
    Optional<EvaluationHistoryEntity> findFirstByOrderBySnapshotDateDesc();

    /**
     * 같은 날 재평가 시 기존 스냅샷 삭제 (멱등 저장용)
     * - 벌크 DELETE로 즉시 실행 → 이어지는 insert보다 먼저 수행 (Hibernate insert-before-delete 순서 이슈 회피)
     */
    @Modifying
    @Query("DELETE FROM EvaluationHistoryEntity e WHERE e.snapshotDate = :snapshotDate")
    void deleteSnapshot(@Param("snapshotDate") LocalDate snapshotDate);
}
