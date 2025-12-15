package com.finance.dart.member.repository;

import com.finance.dart.member.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;


public interface MemberRepository extends JpaRepository<MemberEntity, Long>, JpaSpecificationExecutor<MemberEntity> {

    /**
     * 로그인 정보 확인
     * @param username
     * @return
     */
    MemberEntity findByUsername(String username);

    /**
     * 회원정보 조회(이메일)
     * @param email
     * @return
     */
    List<MemberEntity> findByEmail(String email);

    /**
     * 회원정보 조회(유저명, 이메일)
     * @param username
     * @param email
     * @return
     */
    MemberEntity findByUsernameAndEmail(String username, String email);

}

