package com.finance.dart.mail.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MailSendDto {

    @NotBlank
    private String to;

    @NotBlank
    private String subject;

    @NotNull
    private String contents;

}
