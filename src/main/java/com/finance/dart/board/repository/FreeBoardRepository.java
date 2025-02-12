package com.finance.dart.board.repository;

import com.finance.dart.board.entity.FreeBoard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FreeBoardRepository extends JpaRepository<FreeBoard, Long> {
    // 필요한 커스텀 메서드 추가 가능
}

