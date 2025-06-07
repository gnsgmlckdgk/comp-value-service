package com.finance.dart.member.service;

import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.common.util.StringUtil;
import com.finance.dart.member.entity.Member;
import com.finance.dart.member.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Slf4j
@AllArgsConstructor
@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * 회원가입
     * @param member
     * @return
     */
    public CommonResponse<Member> join(Member member) {

        CommonResponse<Member> commonResponse = new CommonResponse();

        //@ 중복체크
        Member alreadyMember = memberRepository.findByUsername(member.getUsername());
        if(alreadyMember != null) {
            commonResponse.setResponeInfo(ResponseEnum.JOIN_DUPLICATE_USERNAME);
            return commonResponse;
        }

        //@ 비밀번호 암호화
        String encryptedPw = passwordEncoder.encode(member.getPassword());
        member.setPassword(encryptedPw);

        //@ 회원가입
        Member joinMember = memberRepository.save(member);
        joinMember.setPassword(null); // 비밀번호 입력값은 삭제

        commonResponse.setResponse(joinMember);

        return commonResponse;
    }

}
