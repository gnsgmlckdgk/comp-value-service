package com.finance.dart.member.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class LoginDTO {

    private Long id;

    @NotBlank
    private String username;

    @NotBlank
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String password;

    //private String sessionKey;
    private String nickname = "";
    private String email = "";
    private List<String> roles;
    private Long sessionTTL;    // 로그인세션남은시간

    @JsonIgnore
    public static final String redisSessionPrefix = "session:";


    /**
     * 세션 Redis Key GET
     * @param sessionId
     * @return
     */
    public static String getSessionRedisKey(String sessionId) {
        return LoginDTO.redisSessionPrefix + sessionId;
    }
}
