package com.finance.dart.member.service;

import com.finance.dart.common.config.AppProperties;
import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.common.exception.UnauthorizedException;
import com.finance.dart.common.service.RedisComponent;
import com.finance.dart.common.util.StringUtil;
import com.finance.dart.member.dto.LoginDTO;
import com.finance.dart.member.entity.Member;
import com.finance.dart.member.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Slf4j
@AllArgsConstructor
@Service
public class SessionService {

    private final MemberRepository memberRepository;
    private final RedisComponent redisComponent;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    private final int TIMEOUT_MINUTES = 30;


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
        String redisKey = LoginDTO.redisSessionPrefix + sessionKey;
        if(log.isDebugEnabled()) log.debug("redisKey = {}", redisKey);

        redisComponent.saveValueWithTtl(redisKey, StringUtil.defaultString(member.getId()), TIMEOUT_MINUTES, TimeUnit.MINUTES);
        loginDTO.setSessionKey(sessionKey);
        loginDTO.setPassword(null);   // 비밀번호 입력값은 삭제
        loginDTO.setNickName(member.getNickname());

        //@ 응답 조립
        response.setResponse(loginDTO);

        return response;
    }

    /**
     * 세션 키로 쿠키 설정
     * @param sessionKey
     * @return
     */
    public ResponseCookie createSessionCookie(String sessionKey) {

        ResponseCookie cookie = ResponseCookie.from("SESSION_ID", sessionKey)
                .path("/")
                .httpOnly(true)                     // 브라우저에서 쿠키 조회 X
                .secure(!appProperties.isLocal())   // https 에서만 전송 (local은 false)
                .sameSite("Lax")                    // GET/링크/리디렉션 허용, POST는 안함
                .maxAge(Duration.ofMinutes(TIMEOUT_MINUTES))
                .build();

        return cookie;
    }

    /**
     * 로그아웃
     * @param request
     * @return 삭제한 세션ID
     */
    public String logout(HttpServletRequest request) {

        String sessionId = getSessionId(request);

        String redisKey = LoginDTO.redisSessionPrefix + sessionId;
        redisComponent.deleteKey(redisKey);

        return sessionId;
    }

    /**
     * 세션 키로 설정된 쿠키 삭제
     * @param sessionKey
     * @return
     */
    public ResponseCookie deleteSessionCookie(String sessionKey) {
        return ResponseCookie.from("SESSIONID", "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .build();
    }

    /**
     * 세션 키 추출(쿠키)
     * @param request
     * @return
     */
    public String getSessionId(HttpServletRequest request) {

        String sessionId = null;

        // 쿠키에서 세션ID 추출
        if (request.getCookies() != null) {
            for (var cookie : request.getCookies()) {
                if ("SESSION_ID".equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                }
            }
        }

        return sessionId;
    }

    /**
     * 로그인 세션 확인 및 오류응답
     * @param request
     * @param response
     */
    public void sessionCheckErrResponse(HttpServletRequest request, HttpServletResponse response) {

        // 쿠키에서 세션ID 추출
        String sessionId = getSessionId(request);

        if (sessionId == null || !isValidSession(sessionId)) {

            if(sessionId != null) {
                // 만료된 쿠키 삭제
                Cookie deleteCookie = new Cookie("SESSION_ID", "");
                deleteCookie.setPath("/");
                deleteCookie.setMaxAge(0);
                deleteCookie.setHttpOnly(true);
                response.addCookie(deleteCookie);
            }

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            throw new UnauthorizedException(ResponseEnum.LOGIN_SESSION_EXPIRED.getMessage());
        }
    }

    /**
     * 로그인 세션 확인
     * @param request
     * @return true: 로그인인증정상
     */
    public boolean sessionCheck(HttpServletRequest request) {
        String sessionId = getSessionId(request);
        if (sessionId == null || !isValidSession(sessionId)) {
            return false;
        }
        return true;
    }

    private boolean isValidSession(String sessionId) {

        String redisKey = LoginDTO.redisSessionPrefix + sessionId;
        String value = redisComponent.getValue(redisKey);

        if(StringUtil.isStringEmpty(value)) return false;

        return true;
    }

}
