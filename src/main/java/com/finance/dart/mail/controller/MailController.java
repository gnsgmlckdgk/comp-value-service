package com.finance.dart.mail.controller;

import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.mail.dto.MailSendDto;
import com.finance.dart.mail.service.MailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/mail")
public class MailController {

    private final MailService mailService;

    // TODO: 개발중

    @PostMapping("/send")
    public ResponseEntity<CommonResponse> send(@Valid @RequestBody MailSendDto body) {
        //String code = String.valueOf((int)(Math.random() * 900000) + 100000);
        boolean isSend = mailService.sendEmail(body.getTo(), body.getSubject(), body.getContents());

        return new ResponseEntity<>(new CommonResponse(isSend), HttpStatus.OK);
    }

    @PostMapping("/send/html")
    public ResponseEntity<CommonResponse> sendHtml(@Valid @RequestBody MailSendDto body) {
        //String code = String.valueOf((int)(Math.random() * 900000) + 100000);
        boolean isSend = mailService.sendHtmlMail(body.getTo(), body.getSubject(), body.getContents());
        return new ResponseEntity<>(new CommonResponse(isSend), HttpStatus.OK);
    }

}
