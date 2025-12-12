package com.finance.dart.member.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberApproval {
    private Long id;
    private String approvalStatus;
    private String updatedAt;

}
