package com.finance.dart.member.repository;

import com.finance.dart.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * 로그인 정보 확인
     * @param username
     * @return
     */
    Member findByUsername(String username);

}

