package com.finance.dart.member.controller;


import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.member.dto.LoginDTO;
import com.finance.dart.member.dto.Member;
import com.finance.dart.member.entity.MemberEntity;
import com.finance.dart.member.service.MemberService;
import com.finance.dart.member.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
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

    /**
     * 로그아웃
     * @param request
     * @return
     */
    @DeleteMapping("/logout")
    public ResponseEntity<CommonResponse> logout(HttpServletRequest request) {

        String sessionId = sessionService.logout(request);

        ResponseCookie cookie = sessionService.deleteSessionCookie(sessionId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new CommonResponse<>(ResponseEnum.OK));
    }

    /**
     * 회원가입
     * @param memberEntity
     * @return
     */
    @PostMapping("/join")
    public ResponseEntity<CommonResponse<MemberEntity>> join(@Valid @RequestBody MemberEntity memberEntity) {

        CommonResponse<MemberEntity> response = memberService.join(memberEntity);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * <pre>
     * 로그인 여부 체크
     * </pre>
     * @return
     */
    @GetMapping("/me")
    public ResponseEntity<CommonResponse> me(HttpServletRequest request) {

        CommonResponse response = null;
        if(sessionService.sessionCheck(request)) {
            response = new CommonResponse(ResponseEnum.OK);
        } else {
            response = new CommonResponse(ResponseEnum.LOGIN_SESSION_EXPIRED);
        }

        return new ResponseEntity<>(response, ResponseEnum.OK.getHttpStatus());
    }

    /**
     * <pre>
     * 현재 로그인한 회원 정보 조회
     * </pre>
     * @param request
     * @return
     */
    @GetMapping("/me/info")
    public ResponseEntity<CommonResponse> getLoginMember(HttpServletRequest request) {

        Member member = memberService.getLoginMember(request);
        CommonResponse response = new CommonResponse(member);

        return new ResponseEntity<>(response, ResponseEnum.OK.getHttpStatus());
    }

}
