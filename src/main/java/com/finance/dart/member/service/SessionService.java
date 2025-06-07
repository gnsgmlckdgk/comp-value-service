package com.finance.dart.member.service;

import com.finance.dart.common.service.RedisService;
import com.finance.dart.common.util.StringUtil;
import com.finance.dart.member.dto.LoginDTO;
import com.finance.dart.member.entity.Member;
import com.finance.dart.member.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Slf4j
@AllArgsConstructor
@Service
public class LoginService {

    private final MemberRepository memberRepository;
    private final RedisService redisService;



    public String login(LoginDTO loginDTO) {

        Member member = memberRepository.findByUsernameAndPassword(loginDTO.getUsername(), loginDTO.getPassword());
        if(member == null) {
            return "";
        }

        String sessionKey = UUID.randomUUID().toString();
        String redisKey = "session:" + sessionKey;
        redisService.saveValueWithTtl(redisKey, StringUtil.defaultString(member.getId()), 30, TimeUnit.MINUTES);

        return sessionKey;
    }


}
