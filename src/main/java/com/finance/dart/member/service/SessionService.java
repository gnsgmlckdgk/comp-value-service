package com.finance.dart.member.service;

import com.finance.dart.common.component.RedisComponent;
import com.finance.dart.common.config.AppProperties;
import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.common.exception.BizException;
import com.finance.dart.common.exception.UnauthorizedException;
import com.finance.dart.common.util.StringUtil;
import com.finance.dart.member.dto.LoginDTO;
import com.finance.dart.member.entity.MemberEntity;
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
import java.util.List;
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

        //@ 세션정보 저장
        String sessionKey = UUID.randomUUID().toString();
        String redisKey = LoginDTO.redisSessionPrefix + sessionKey;
        if(log.isDebugEnabled()) log.debug("redisKey = {}", redisKey);

        redisComponent.saveValueWithTtl(redisKey, StringUtil.defaultString(memberEntity.getId()), TIMEOUT_MINUTES, TimeUnit.MINUTES);
        loginDTO.setSessionKey(sessionKey);
        loginDTO.setPassword(null);   // 비밀번호 입력값은 삭제
        loginDTO.setNickName(memberEntity.getNickname());

        // 권한 정보 조회 및 Redis 저장
        CommonResponse<List<String>> memRoleListRes = roleService.getMemberRoles(memberEntity.getId());
        List<String> memRoleList = memRoleListRes.getResponse();
        loginDTO.setRoles(memRoleList);

        // 권한 정보 Redis 저장 (memberId 기준)
        String rolesRedisKey = LoginDTO.redisRolesPrefix + memberEntity.getId();
        String rolesValue = String.join(",", memRoleList != null ? memRoleList : List.of());
        redisComponent.saveValueWithTtl(rolesRedisKey, rolesValue, TIMEOUT_MINUTES, TimeUnit.MINUTES);

        //@ 응답 조립
        // 세션 TTL 정보
        loginDTO.setSessionTTL(getLoginSessionTTL(sessionKey));
        loginDTO.setRolesTTL(getAuthSessionTTL(memberEntity.getId()));

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

        // 세션에서 memberId 조회 후 권한 정보도 삭제
        String sessionRedisKey = LoginDTO.redisSessionPrefix + sessionId;
        String memberId = redisComponent.getValue(sessionRedisKey);
        if (memberId != null) {
            String rolesRedisKey = LoginDTO.redisRolesPrefix + memberId;
            redisComponent.deleteKey(rolesRedisKey);
        }

        redisComponent.deleteKey(sessionRedisKey);

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
        } else {
            // TTL 갱신 제외 URL 체크
            String requestUri = request.getRequestURI();
            if (!isExcludedFromTtlRefresh(requestUri)) {
                // 세션 관련 모든 Redis 키 TTL 일괄 갱신
                refreshSessionTtl(sessionId);
            }
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
    private boolean isExcludedFromTtlRefresh(String requestUri) {
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

        String redisKey = LoginDTO.redisSessionPrefix + sessionId;
        String value = redisComponent.getValue(redisKey);

        if(StringUtil.isStringEmpty(value)) return false;

        return true;
    }

    /**
     * 로그인 세션 TTL 확인
     * @param sessionKey
     * @return -1 : TTL 설정안되어있는 키
     *         -2 : 키가 없음
     */
    public long getLoginSessionTTL(String sessionKey) {

        String redisKey = LoginDTO.redisSessionPrefix + sessionKey;
        Long ttl = redisComponent.getTTL(redisKey);

        return ttl.longValue();
    }

    /**
     * 권한 세션 TTL 확인
     * @param memberId
     * @return -1 : TTL 설정안되어있는 키
     *         -2 : 키가 없음
     */
    public long getAuthSessionTTL(long memberId) {

        String authRedisKey = LoginDTO.redisRolesPrefix + memberId;
        Long ttl = redisComponent.getTTL(authRedisKey);

        return ttl.longValue();
    }


    /**
     * 세션 관련 모든 Redis 키의 TTL을 일괄 갱신
     * - 로그인 세션 정보 (session:{sessionId})
     * - 권한 정보 (roles:{memberId})
     * @param sessionId 세션 ID
     */
    public void refreshSessionTtl(String sessionId) {
        if (sessionId == null) return;

        // 세션 키에서 memberId 조회
        String memberId = redisComponent.getValue(LoginDTO.redisSessionPrefix + sessionId);
        if (memberId == null) return;

        // 세션 TTL 갱신
        redisComponent.updateTtl(LoginDTO.redisSessionPrefix + sessionId, TIMEOUT_MINUTES, TimeUnit.MINUTES);

        // 권한 정보 TTL 갱신
        redisComponent.updateTtl(LoginDTO.redisRolesPrefix + memberId, TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Redis에서 현재 로그인한 사용자의 권한 목록 조회
     * @param request
     * @return 권한 목록 (없으면 빈 리스트)
     */
    public List<String> getRolesFromSession(HttpServletRequest request) {
        String sessionId = getSessionId(request);
        if (sessionId == null) {
            return List.of();
        }

        String memberId = redisComponent.getValue(LoginDTO.redisSessionPrefix + sessionId);
        if (memberId == null) {
            return List.of();
        }

        String rolesValue = redisComponent.getValue(LoginDTO.redisRolesPrefix + memberId);
        if (StringUtil.isStringEmpty(rolesValue)) {
            return List.of();
        }

        return List.of(rolesValue.split(","));
    }

    /**
     * 사용자 권한 갱신
     * @param request
     * @param userRoles
     */
    public void updateLoginRolesTTL(HttpServletRequest request, List<String> userRoles) {

        String sessionId = getSessionId(request);
        if (sessionId == null) {
            throw new BizException(ResponseEnum.LOGIN_SESSION_EXPIRED.getMessage());
        }

        String memberId = redisComponent.getValue(LoginDTO.redisSessionPrefix + sessionId);
        if (memberId == null) {
            throw new BizException(ResponseEnum.LOGIN_SESSION_EXPIRED.getMessage());
        }

        // 권한정보 갱신
        String userRolesData = String.join(",", userRoles != null ? userRoles : List.of());
        redisComponent.saveValueWithTtl(LoginDTO.redisRolesPrefix + memberId, userRolesData, TIMEOUT_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * 현재 로그인한 사용자가 특정 권한을 가지고 있는지 확인
     * @param request
     * @param roleName
     * @return
     */
    public boolean hasRole(HttpServletRequest request, String roleName) {
        List<String> roles = getRolesFromSession(request);
        return roles.contains(roleName);
    }

}
