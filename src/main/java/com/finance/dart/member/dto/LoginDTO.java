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

    @JsonIgnore
    public static final String redisSessionPrefix = "session:";

    @JsonIgnore
    public static final String redisRolesPrefix = "roles:";
}
