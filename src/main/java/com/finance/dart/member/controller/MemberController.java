package com.finance.dart.member.controller;


import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.member.dto.*;
import com.finance.dart.member.enums.Role;
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
     * 현재 로그인 계정 관리자 권한 여부 확인
     * @param request
     * @return
     */
    @GetMapping("/admin/users")
    public ResponseEntity<?> getUsers01(HttpServletRequest request) {

        CommonResponse response = new CommonResponse();

        if (!sessionService.hasRole(request, Role.ADMIN.getRoleName())) {
            return ResponseEntity.status(403).body("권한 없음");
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 현재 로그인 계정 슈퍼 관리자 권한 여부 확인
     * @param request
     * @return
     */
    @GetMapping("/super_admin/users")
    public ResponseEntity<?> getUsers02(HttpServletRequest request) {

        CommonResponse response = new CommonResponse();

        if (!sessionService.hasRole(request, Role.SUPER_ADMIN.getRoleName())) {
            return ResponseEntity.status(403).body("권한 없음");
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
     * @param memberJoinReqDto
     * @return
     */
    @PostMapping("/join")
    public ResponseEntity<CommonResponse<Member>> join(@Valid @RequestBody MemberJoinReqDto memberJoinReqDto) {

        CommonResponse<Member> response = memberService.join(memberJoinReqDto);

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

    /**
     * 회원정보 수정
     * @param request
     * @param reqBody
     * @return
     */
    @PostMapping("/update")
    public ResponseEntity<CommonResponse<Member>> updateMember(HttpServletRequest request, @RequestBody Member reqBody) {

        CommonResponse<Member> response = memberService.updateMember(request, reqBody);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 비밀번호 변경
     * @param request
     * @param reqBody
     * @return
     */
    @PostMapping("/password")
    public ResponseEntity<CommonResponse<Void>> changePassword(HttpServletRequest request, @Valid @RequestBody PasswordChangeDto reqBody) {

        CommonResponse<Void> response = memberService.changePassword(request, reqBody.getCurrentPassword(), reqBody.getNewPassword());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 회원 탈퇴
     * @param request
     * @param reqBody
     * @return
     */
    @PostMapping("/delete")
    public ResponseEntity<CommonResponse<Void>> deleteMember(HttpServletRequest request, @Valid @RequestBody MemberDeleteDto reqBody) {

        CommonResponse<Void> response = memberService.deleteMember(request, reqBody.getPassword());

        // 세션 쿠키 삭제
        String sessionId = sessionService.getSessionId(request);
        ResponseCookie cookie = sessionService.deleteSessionCookie(sessionId);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

}
