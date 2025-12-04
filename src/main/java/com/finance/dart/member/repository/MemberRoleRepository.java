package com.finance.dart.member.repository;

import com.finance.dart.member.entity.MemberRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberRoleRepository extends JpaRepository<MemberRoleEntity, Long> {

    List<MemberRoleEntity> findByMemberId(Long memberId);

    void deleteByMemberIdAndRoleId(Long memberId, Long roleId);
}
