package com.finance.dart.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class LoginDTO {

    @NotBlank
    private String username;

    @NotBlank
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    private String sessionKey;

    private String nickName = "";

    private List<String> roles;

    private Long sessionTTL;    // 로그인세션남은시간
    private Long rolesTTL;      // 권한세션남은시간

    @JsonIgnore
    public static final String redisSessionPrefix = "session:";

    @JsonIgnore
    public static final String redisRolesPrefix = "roles:";

    /**
     * 세션 갱신 시 TTL 갱신이 필요한 Redis 키 prefix 목록
     */
    @JsonIgnore
    public static final String[] SESSION_TTL_REFRESH_PREFIXES = {
            redisSessionPrefix,  // 로그인 세션 정보
            redisRolesPrefix     // 권한 정보
    };
}
