package com.finance.dart.member.service;

import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.common.service.RedisService;
import com.finance.dart.common.util.StringUtil;
import com.finance.dart.member.dto.LoginDTO;
import com.finance.dart.member.entity.Member;
import com.finance.dart.member.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Slf4j
@AllArgsConstructor
@Service
public class SessionService {

    private final MemberRepository memberRepository;
    private final RedisService redisService;
    private final PasswordEncoder passwordEncoder;
    private final String redisKeyPre = "session:";  // 세션 키 접두어


    /**
     * 로그인 및 세션정보 저장
     * @param loginDTO
     * @return "" : 로그인 실패, "세션키" : 로그인 성공
     */
    public CommonResponse<LoginDTO> login(LoginDTO loginDTO) {

        CommonResponse<LoginDTO> response = new CommonResponse<>();

        //@ 로그인 정보 확인
        Member member = memberRepository.findByUsername(loginDTO.getUsername());
        if(member == null) {
            response.setResponeInfo(ResponseEnum.LOGIN_NOTFOUND_USER);
            return response;
        }
        //@ 비밀번호 비교
        if (!passwordEncoder.matches(loginDTO.getPassword(), member.getPassword())) {
            response.setResponeInfo(ResponseEnum.LOGIN_NOTMATCH_PASSWORD);
            return response;
        }

        //@ 세션정보 저장
        String sessionKey = UUID.randomUUID().toString();
        String redisKey = redisKeyPre + sessionKey;
        if(log.isDebugEnabled()) log.debug("redisKey = {}", redisKey);

        redisService.saveValueWithTtl(redisKey, StringUtil.defaultString(member.getId()), 30, TimeUnit.MINUTES);
        loginDTO.setSessionKey(sessionKey);
        loginDTO.setPassword(null);   // 비밀번호 입력값은 삭제

        //@ 응답 조립
        response.setResponse(loginDTO);

        return response;
    }


}
