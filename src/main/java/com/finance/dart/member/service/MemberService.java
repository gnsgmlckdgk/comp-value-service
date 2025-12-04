package com.finance.dart.member.service;

import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.common.service.RedisComponent;
import com.finance.dart.common.util.ConvertUtil;
import com.finance.dart.member.dto.LoginDTO;
import com.finance.dart.member.dto.Member;
import com.finance.dart.member.entity.MemberEntity;
import com.finance.dart.member.entity.MemberRoleEntity;
import com.finance.dart.member.entity.RoleEntity;
import com.finance.dart.member.enums.Role;
import com.finance.dart.member.repository.MemberRepository;
import com.finance.dart.member.repository.MemberRoleRepository;
import com.finance.dart.member.repository.RoleRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;


@Slf4j
@AllArgsConstructor
@Service
public class MemberService {

    private final SessionService sessionService;
    private final RedisComponent redisComponent;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;
    private final MemberRoleRepository memberRoleRepository;


    /**
     * 로그인한 회원 정보 조회
     * @param request
     * @return
     */
    public Member getLoginMember(HttpServletRequest request) {

        String sessionId = sessionService.getSessionId(request);

        String memberIdStr = redisComponent.getValue(LoginDTO.redisSessionPrefix + sessionId);
        Long memberId = Long.parseLong(memberIdStr);

        return getMember(memberId);
    }

    /**
     * 회원 조회
     * @param memberId
     * @return
     */
    public Member getMember(Long memberId) {

        Optional<MemberEntity> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            return null;
        }

        MemberEntity memberEntity = memberOpt.get();

        // Gson 변환 실패 등으로 인한 NPE 방지를 위해 수동 매핑
        Member member = new Member();
        member.setId(memberEntity.getId());
        member.setUsername(memberEntity.getUsername());
        member.setEmail(memberEntity.getEmail());
        member.setNickname(memberEntity.getNickname());
        if (memberEntity.getCreatedAt() != null) {
            member.setCreatedAt(memberEntity.getCreatedAt().toString());
        }
        if (memberEntity.getUpdatedAt() != null) {
            member.setUpdatedAt(memberEntity.getUpdatedAt().toString());
        }

        return member;
    }

    /**
     * 회원가입
     * @param memberEntity
     * @return
     */
    public CommonResponse<Member> join(MemberEntity memberEntity) {

        CommonResponse<Member> commonResponse = new CommonResponse<>();

        //@ 중복체크
        MemberEntity alreadyMemberEntity = memberRepository.findByUsername(memberEntity.getUsername());
        if(alreadyMemberEntity != null) {
            commonResponse.setResponeInfo(ResponseEnum.JOIN_DUPLICATE_USERNAME);
            return commonResponse;
        }

        //@ 비밀번호 암호화
        String encryptedPw = passwordEncoder.encode(memberEntity.getPassword());
        memberEntity.setPassword(encryptedPw);

        //@ 회원가입
        MemberEntity joinMemberEntity = memberRepository.save(memberEntity);

        //@ 기본 권한 설정
        Optional<RoleEntity> optRole = roleRepository.findByRoleName(Role.USER.getRoleName());
        if(optRole.isPresent()) {
            MemberRoleEntity mre = new MemberRoleEntity();
            mre.setMember(joinMemberEntity);
            mre.setRole(optRole.get());
            memberRoleRepository.save(mre);
        }

        //@ Entity -> DTO 변환 (비밀번호는 자동으로 제외됨)
        Member member = ConvertUtil.parseObject(joinMemberEntity, Member.class);
        commonResponse.setResponse(member);

        return commonResponse;
    }

}
