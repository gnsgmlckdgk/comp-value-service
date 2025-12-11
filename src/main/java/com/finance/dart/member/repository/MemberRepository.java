package com.finance.dart.member.repository;

import com.finance.dart.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface MemberRepository extends JpaRepository<MemberEntity, Long>, JpaSpecificationExecutor<MemberEntity> {

    /**
     * 로그인 정보 확인
     * @param username
     * @return
     */
    MemberEntity findByUsername(String username);

}

