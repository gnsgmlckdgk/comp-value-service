package com.finance.dart.member.repository;

import com.finance.dart.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface MemberRepository extends JpaRepository<MemberEntity, Long> {

    /**
     * 로그인 정보 확인
     * @param username
     * @return
     */
    MemberEntity findByUsername(String username);

}

