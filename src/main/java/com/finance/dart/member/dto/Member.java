package com.finance.dart.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Member {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String createdAt;
    private String updatedAt;
    private List<String> roles;
}
