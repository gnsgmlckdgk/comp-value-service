package com.finance.dart.member.controller;


import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.member.dto.LoginDTO;
import com.finance.dart.member.entity.Member;
import com.finance.dart.member.service.MemberService;
import com.finance.dart.member.service.SessionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@AllArgsConstructor
@RequestMapping("member")
@RestController
public class MemberController {

    private final SessionService sessionService;
    private final MemberService memberService;

    // TODO: 회원정보 수정, 삭제 기능 추가 예정
    // TODO: 로그아웃 기능 추가 예정

    /**
     * 로그인
     * @param loginDTO
     * @return "" 응답은 로그인 실패
     */
    @PostMapping("/login")
    public ResponseEntity<CommonResponse<LoginDTO>> login(@Valid @RequestBody LoginDTO loginDTO) {

        CommonResponse<LoginDTO> response = sessionService.login(loginDTO);

        if(response != null && response.getResponse() != null) {
            // 쿠키 설정
            ResponseCookie cookie = sessionService.createSessionCookie(response.getResponse().getSessionKey());
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(response);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/logout")
    public ResponseEntity<CommonResponse> logout(@RequestParam("sessionId") String sessionId) {

        sessionService.logout(sessionId);

        ResponseCookie cookie = sessionService.deleteSessionCookie(sessionId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new CommonResponse<>(ResponseEnum.OK));
    }

    /**
     * 회원가입
     * @param member
     * @return
     */
    @PostMapping("/join")
    public ResponseEntity<CommonResponse<Member>> join(@Valid @RequestBody Member member) {

        CommonResponse<Member> response = memberService.join(member);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
