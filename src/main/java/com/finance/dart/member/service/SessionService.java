package com.finance.dart.member.service;

import com.finance.dart.api.common.constants.RequestContextConst;
import com.finance.dart.api.common.context.RequestContext;
import com.finance.dart.common.component.RedisComponent;
import com.finance.dart.common.config.AppProperties;
import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.common.exception.UnauthorizedException;
import com.finance.dart.common.util.ConvertUtil;
import com.finance.dart.common.util.StringUtil;
import com.finance.dart.member.dto.LoginDTO;
import com.finance.dart.member.entity.MemberEntity;
import com.finance.dart.member.repository.MemberRepository;
import com.google.gson.Gson;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Slf4j
@AllArgsConstructor
@Service
public class SessionService {

    private final RequestContext requestContext;
    private final MemberRepository memberRepository;
    private final RedisComponent redisComponent;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;
    private final RoleService roleService;

    private final int TIMEOUT_MINUTES = 30;


    /**
     * 로그인 및 세션정보 저장
     * @param loginDTO
     * @return "" : 로그인 실패, "세션키" : 로그인 성공
     */
    public CommonResponse<LoginDTO> login(LoginDTO loginDTO) {

        CommonResponse<LoginDTO> response = new CommonResponse<>();

        //@ 로그인 정보 확인
        MemberEntity memberEntity = memberRepository.findByUsername(loginDTO.getUsername());
        if(memberEntity == null) {
            response.setResponeInfo(ResponseEnum.LOGIN_NOTFOUND_USER);
            return response;
        }
        //@ 비밀번호 비교
        if (!passwordEncoder.matches(loginDTO.getPassword(), memberEntity.getPassword())) {
            response.setResponeInfo(ResponseEnum.LOGIN_NOTMATCH_PASSWORD);
            return response;
        }

        //@ 회원 승인 상태 확인
        if (!"Y".equals(memberEntity.getApprovalStatus())) {
            response.setResponeInfo(ResponseEnum.MEMBER_NOT_APPROVED);
            return response;
        }

        //@ 세션키 세팅
        String sessionId = UUID.randomUUID().toString();
        requestContext.setAttribute(RequestContextConst.SESSION_ID_UUID, sessionId);
        String redisKey = LoginDTO.getSessionRedisKey(sessionId);

        //@ 권한 정보 조회
        CommonResponse<List<String>> memRoleListRes = roleService.getMemberRoles(memberEntity.getId());
        List<String> memRoleList = memRoleListRes.getResponse();

        loginDTO.setId(memberEntity.getId());
        loginDTO.setEmail(memberEntity.getEmail());
        loginDTO.setRoles(memRoleList);
        //loginDTO.setSessionKey(sessionId);
        loginDTO.setPassword(null);   // 비밀번호 입력값은 삭제
        loginDTO.setNickname(memberEntity.getNickname());

        // 세션 TTL 정보
        redisComponent.saveValueWithTtl(redisKey, new Gson().toJson(loginDTO), TIMEOUT_MINUTES, TimeUnit.MINUTES);

        loginDTO.setSessionTTL(getLoginSessionTTL(sessionId));
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
                // Lax 는 쿠키 갱신이 안되서 토큰 방식으로 변경예정
                // None 으로 바꾸면 되나 secure(true) 설정 필수 : https 만 생성 및 갱신 가능
                .sameSite(appProperties.isLocal() ? "Lax" : "None")
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
        redisComponent.deleteKey(LoginDTO.getSessionRedisKey(sessionId));

        return sessionId;
    }

    /**
     * 세션 키로 설정된 쿠키 삭제
     * @param sessionKey
     * @return
     */
    public ResponseCookie deleteSessionCookie(String sessionKey) {
        return ResponseCookie.from("SESSION_ID", "")
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
     * 로그인 정보 조회
     * @param request
     * @return
     */
    public LoginDTO getLoginInfo(HttpServletRequest request) {

        String sessionId = getSessionId(request);
        if(StringUtil.isStringEmpty(sessionId)) return null;

        String loginDtoStr = redisComponent.getValue(LoginDTO.getSessionRedisKey(sessionId));
        if(StringUtil.isStringEmpty(loginDtoStr)) return null;

        LoginDTO loginDTO = ConvertUtil.parseObject(loginDtoStr, LoginDTO.class);

        return loginDTO;
    }

    /**
     * 로그인 세션 확인만 수행 (TTL 갱신 없음)
     * - TTL 갱신은 Interceptor 후처리에서 쿠키와 함께 수행
     * @param request
     * @param response
     */
    public void sessionCheckOnly(HttpServletRequest request, HttpServletResponse response) {

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
     * TTL 갱신에서 제외할 URL 목록
     */
    private static final List<String> TTL_REFRESH_EXCLUDE_URLS = List.of(
            "/member/me/info"
    );

    /**
     * TTL 갱신 제외 URL인지 확인
     * @param requestUri 요청 URI
     * @return true: 갱신 제외
     */
    public boolean isExcludedFromTtlRefresh(String requestUri) {
        return TTL_REFRESH_EXCLUDE_URLS.stream()
                .anyMatch(requestUri::endsWith);
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

        String redisKey = LoginDTO.getSessionRedisKey(sessionId);
        String value = redisComponent.getValue(redisKey);

        if(StringUtil.isStringEmpty(value)) return false;

        return true;
    }

    /**
     * 로그인 세션 TTL 확인
     * @param sessionId
     * @return -1 : TTL 설정안되어있는 키
     *         -2 : 키가 없음
     */
    public long getLoginSessionTTL(String sessionId) {

        String redisKey = LoginDTO.getSessionRedisKey(sessionId);
        Long ttl = redisComponent.getTTL(redisKey);

        return ttl.longValue();
    }

    /**
     * 세션 관련 모든 Redis 키의 TTL을 일괄 갱신
     * - 로그인 세션 정보 (session:{sessionId})
     * @param sessionId 세션 ID
     */
    public void refreshSessionTtl(String sessionId) {
        if (sessionId == null) return;

        // 세션 키에서 memberId 조회
        String loginDtoStr = redisComponent.getValue(LoginDTO.getSessionRedisKey(sessionId));
        if (loginDtoStr == null) return;

        // 세션 TTL 갱신
        redisComponent.updateTtl(LoginDTO.getSessionRedisKey(sessionId), TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 현재 로그인한 사용자가 특정 권한을 가지고 있는지 확인
     * @param request
     * @param roleName
     * @return
     */
    public boolean hasRole(HttpServletRequest request, String roleName) {

        LoginDTO loginDTO = getLoginInfo(request);

        if (loginDTO == null || loginDTO.getRoles() == null) {
            return false;
        }

        List<String> roles = loginDTO.getRoles();

        return roles.contains(roleName);
    }

}
