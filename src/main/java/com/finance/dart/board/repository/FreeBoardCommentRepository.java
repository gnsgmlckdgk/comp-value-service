package com.finance.dart.board.repository;

import com.finance.dart.board.entity.FreeBoardComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FreeBoardCommentRepository extends JpaRepository<FreeBoardComment, Long> {

    List<FreeBoardComment> findByFreeBoardIdAndParentIsNullOrderByCreatedAtAsc(Long freeBoardId);

    int countByFreeBoardIdAndDeletedFalse(Long freeBoardId);
}
