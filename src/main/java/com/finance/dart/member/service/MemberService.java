package com.finance.dart.member.service;

import com.finance.dart.common.service.RedisService;
import com.finance.dart.common.util.StringUtil;
import com.finance.dart.member.dto.LoginDTO;
import com.finance.dart.member.entity.Member;
import com.finance.dart.member.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Slf4j
@AllArgsConstructor
@Service
public class SessionService {

    private final MemberRepository memberRepository;
    private final RedisService redisService;

    private final String redisKeyPre = "session:";  // 세션 키 접두어


    /**
     * 로그인
     * @param loginDTO
     * @return "" : 로그인 실패, "세션키" : 로그인 성공
     */
    public String login(LoginDTO loginDTO) {

        Member member = memberRepository.findByUsernameAndPassword(loginDTO.getUsername(), loginDTO.getPassword());
        if(member == null) {
            return "";
        }

        String sessionKey = UUID.randomUUID().toString();
        String redisKey = redisKeyPre + sessionKey;

        redisService.saveValueWithTtl(redisKey, StringUtil.defaultString(member.getId()), 30, TimeUnit.MINUTES);

        return sessionKey;
    }


}
