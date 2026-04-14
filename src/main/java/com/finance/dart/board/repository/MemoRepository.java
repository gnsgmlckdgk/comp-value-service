package com.finance.dart.board.repository;

import com.finance.dart.board.entity.MemoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 개인 메모 Repository
 */
public interface MemoRepository extends JpaRepository<MemoEntity, Long> {

    /**
     * 회원의 전체 메모 조회 (고정 우선, 최신순)
     */
    List<MemoEntity> findByMember_IdOrderByPinnedDescCreatedAtDesc(Long memberId);

    /**
     * 회원의 카테고리별 메모 조회 (고정 우선, 최신순)
     */
    List<MemoEntity> findByMember_IdAndCategoryOrderByPinnedDescCreatedAtDesc(Long memberId, String category);

    /**
     * 회원의 카테고리 목록 조회 (중복 제거)
     */
    @Query("SELECT DISTINCT m.category FROM MemoEntity m WHERE m.member.id = :memberId AND m.category IS NOT NULL ORDER BY m.category")
    List<String> findDistinctCategoryByMember_Id(@Param("memberId") Long memberId);
}
