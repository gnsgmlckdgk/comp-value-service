package com.finance.dart.member.service;

import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.common.component.RedisComponent;
import com.finance.dart.common.util.ConvertUtil;
import com.finance.dart.mail.service.MailService;
import com.finance.dart.member.dto.*;
import com.finance.dart.member.entity.MemberEntity;
import com.finance.dart.member.entity.MemberRoleEntity;
import com.finance.dart.member.entity.RoleEntity;
import com.finance.dart.member.enums.RoleConstants;
import com.finance.dart.member.repository.MemberRepository;
import com.finance.dart.member.repository.MemberRoleRepository;
import com.finance.dart.member.repository.RoleRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;


@Slf4j
@AllArgsConstructor
@Service
public class MemberService {

    private final PasswordEncoder passwordEncoder;

    private final SessionService sessionService;
    private final RoleService roleService;
    private final RedisComponent redisComponent;
    private final MailService mailService;

    private final RoleRepository roleRepository;
    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;

    private static final int VERIFICATION_CODE_MIN = 100000;
    private static final int VERIFICATION_CODE_MAX = 999999;
    private static final long PASSWORD_RESET_TTL_SECONDS = 300L;
    private static final int TEMPORARY_PASSWORD_LENGTH = 12;
    private static final long PASSWORD_RESET_TTL_MINUTES = PASSWORD_RESET_TTL_SECONDS / 60;


    /**
     * 로그인한 회원 정보 조회
     * @param request
     * @return
     */
    public Member getLoginMember(HttpServletRequest request) {

        String sessionId = sessionService.getSessionId(request);

        String memberIdStr = redisComponent.getValue(LoginDTO.redisSessionPrefix + sessionId);
        Long memberId = Long.parseLong(memberIdStr);

        return getMember(memberId);
    }

    /**
     * 회원 조회
     * @param memberId
     * @return
     */
    public Member getMember(Long memberId) {

        Optional<MemberEntity> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            return null;
        }

        MemberEntity memberEntity = memberOpt.get();

        // Gson 변환 실패 등으로 인한 NPE 방지를 위해 수동 매핑
        Member member = new Member();
        member.setId(memberEntity.getId());
        member.setUsername(memberEntity.getUsername());
        member.setEmail(memberEntity.getEmail());
        member.setNickname(memberEntity.getNickname());
        if (memberEntity.getCreatedAt() != null) {
            member.setCreatedAt(memberEntity.getCreatedAt().toString());
        }
        if (memberEntity.getUpdatedAt() != null) {
            member.setUpdatedAt(memberEntity.getUpdatedAt().toString());
        }

        //@ 권한정보 조회
        CommonResponse<List<String>> roleListRes = roleService.getMemberRoles(memberEntity.getId());
        if(roleListRes == null) {
            member.setRoles(new LinkedList<>());
        } else {
            List<String> roleList = roleListRes.getResponse();
            member.setRoles(roleList);
        }

        return member;
    }

    /**
     * 회원가입
     * @param memberEntity
     * @return
     */
//    public CommonResponse<Member> join(MemberEntity memberEntity) {
    public CommonResponse<Member> join(MemberJoinReqDto memberJoinReqDto) {

        CommonResponse<Member> commonResponse = new CommonResponse<>();

        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setUsername(memberJoinReqDto.getUsername());
        memberEntity.setPassword(memberJoinReqDto.getPassword());
        memberEntity.setEmail(memberJoinReqDto.getEmail());
        memberEntity.setNickname(memberJoinReqDto.getNickname());

        //@ 중복체크
        MemberEntity alreadyMemberEntity = memberRepository.findByUsername(memberEntity.getUsername());
        if(alreadyMemberEntity != null) {
            commonResponse.setResponeInfo(ResponseEnum.JOIN_DUPLICATE_USERNAME);
            return commonResponse;
        }

        //@ 비밀번호 암호화
        String encryptedPw = passwordEncoder.encode(memberEntity.getPassword());
        memberEntity.setPassword(encryptedPw);

        //@ 회원가입
        MemberEntity joinMemberEntity = memberRepository.save(memberEntity);

        //@ 기본 권한 설정
        Optional<RoleEntity> optRole = roleRepository.findByRoleName(RoleConstants.ROLE_USER);
        if(optRole.isPresent()) {
            MemberRoleEntity mre = new MemberRoleEntity();
            mre.setMember(joinMemberEntity);
            mre.setRole(optRole.get());
            memberRoleRepository.save(mre);
        }

        //@ Entity -> DTO 변환 (비밀번호는 자동으로 제외됨)
        Member member = ConvertUtil.parseObject(joinMemberEntity, Member.class);
        commonResponse.setResponse(member);

        return commonResponse;
    }

    /**
     * 회원정보 수정
     * @param request
     * @param updateMember
     * @return
     */
    public CommonResponse<Member> updateMember(HttpServletRequest request, Member updateMember) {

        CommonResponse<Member> commonResponse = new CommonResponse<>();

        // 로그인 회원 정보
        Member loginMember = getLoginMember(request);
        if (loginMember == null) {
            commonResponse.setResponeInfo(ResponseEnum.LOGIN_SESSION_EXPIRED);
            return commonResponse;
        }

        // 회원 조회
        Optional<MemberEntity> memberOpt = memberRepository.findById(loginMember.getId());
        if (memberOpt.isEmpty()) {
            commonResponse.setResponeInfo(ResponseEnum.MEMBER_NOT_FOUND);
            return commonResponse;
        }

        MemberEntity memberEntity = memberOpt.get();

        // 수정 가능 필드 업데이트
        if (updateMember.getEmail() != null) {
            memberEntity.setEmail(updateMember.getEmail());
        }
        if (updateMember.getNickname() != null) {
            memberEntity.setNickname(updateMember.getNickname());
        }
        memberEntity.setUpdatedAt(java.time.LocalDateTime.now());

        // 저장
        MemberEntity savedEntity = memberRepository.save(memberEntity);

        // Entity -> DTO 변환
        Member member = getMember(savedEntity.getId());
        commonResponse.setResponse(member);

        return commonResponse;
    }

    /**
     * 회원정보 등록승인여부 수정
     * @param memberApproval
     * @return
     */
    public CommonResponse<MemberApproval> updateMemberApproval(MemberApproval memberApproval) {

        CommonResponse<MemberApproval> commonResponse = new CommonResponse<>();

        // 회원 조회
        Optional<MemberEntity> memberOpt = memberRepository.findById(memberApproval.getId());
        if (memberOpt.isEmpty()) {
            commonResponse.setResponeInfo(ResponseEnum.MEMBER_NOT_FOUND);
            return commonResponse;
        }

        MemberEntity memberEntity = memberOpt.get();

        // 수정 가능 필드 업데이트
        if(memberApproval.getApprovalStatus() != null) {
            memberEntity.setApprovalStatus(memberApproval.getApprovalStatus());
        }
        memberEntity.setUpdatedAt(java.time.LocalDateTime.now());

        // 저장
        MemberEntity savedEntity = memberRepository.save(memberEntity);

        // Entity -> DTO 변환
        Optional<MemberEntity> memberEntityOptional = memberRepository.findById(savedEntity.getId());
        if(memberEntityOptional.isPresent()) {
            MemberEntity updatedEntity = memberEntityOptional.get();

            MemberApproval responseMemberApproval = new MemberApproval();
            responseMemberApproval.setId(updatedEntity.getId());
            responseMemberApproval.setApprovalStatus(updatedEntity.getApprovalStatus());
            responseMemberApproval.setUpdatedAt(updatedEntity.getUpdatedAt().toString());

            commonResponse.setResponse(responseMemberApproval);
        }

        return commonResponse;
    }

    /**
     * 비밀번호 변경
     * @param request
     * @param currentPassword
     * @param newPassword
     * @return
     */
    public CommonResponse<Void> changePassword(HttpServletRequest request, String currentPassword, String newPassword) {

        CommonResponse<Void> commonResponse = new CommonResponse<>();

        // 로그인 회원 정보
        Member loginMember = getLoginMember(request);
        if (loginMember == null) {
            commonResponse.setResponeInfo(ResponseEnum.LOGIN_SESSION_EXPIRED);
            return commonResponse;
        }

        // 회원 조회
        Optional<MemberEntity> memberOpt = memberRepository.findById(loginMember.getId());
        if (memberOpt.isEmpty()) {
            commonResponse.setResponeInfo(ResponseEnum.MEMBER_NOT_FOUND);
            return commonResponse;
        }

        MemberEntity memberEntity = memberOpt.get();

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, memberEntity.getPassword())) {
            commonResponse.setResponeInfo(ResponseEnum.PASSWORD_NOT_MATCH);
            return commonResponse;
        }

        // 새 비밀번호 암호화 및 저장
        String encryptedPw = passwordEncoder.encode(newPassword);
        memberEntity.setPassword(encryptedPw);
        memberEntity.setUpdatedAt(java.time.LocalDateTime.now());

        memberRepository.save(memberEntity);

        return commonResponse;
    }

    /**
     * 회원 탈퇴
     * @param request
     * @param password
     * @return
     */
    public CommonResponse<Void> deleteMember(HttpServletRequest request, String password) {

        CommonResponse<Void> commonResponse = new CommonResponse<>();

        // 로그인 회원 정보
        Member loginMember = getLoginMember(request);
        if (loginMember == null) {
            commonResponse.setResponeInfo(ResponseEnum.LOGIN_SESSION_EXPIRED);
            return commonResponse;
        }

        // 회원 조회
        Optional<MemberEntity> memberOpt = memberRepository.findById(loginMember.getId());
        if (memberOpt.isEmpty()) {
            commonResponse.setResponeInfo(ResponseEnum.MEMBER_NOT_FOUND);
            return commonResponse;
        }

        MemberEntity memberEntity = memberOpt.get();

        // 비밀번호 확인
        if (!passwordEncoder.matches(password, memberEntity.getPassword())) {
            commonResponse.setResponeInfo(ResponseEnum.PASSWORD_NOT_MATCH);
            return commonResponse;
        }

        // 회원 권한 삭제 (cascade로 자동 삭제되지만 명시적으로)
        memberRoleRepository.deleteAll(memberEntity.getMemberRoles());

        // 회원 삭제
        memberRepository.delete(memberEntity);

        // 세션 삭제
        String sessionId = sessionService.getSessionId(request);
        redisComponent.deleteKey(LoginDTO.redisSessionPrefix + sessionId);

        return commonResponse;
    }

    /**
     * 관리자용 회원정보 수정
     * @param memberId
     * @param email
     * @param nickname
     * @return
     */
    public CommonResponse<Member> updateMemberByAdmin(Long memberId, String email, String nickname) {

        CommonResponse<Member> commonResponse = new CommonResponse<>();

        // 회원 조회
        Optional<MemberEntity> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            commonResponse.setResponeInfo(ResponseEnum.MEMBER_NOT_FOUND);
            return commonResponse;
        }

        MemberEntity memberEntity = memberOpt.get();

        // 수정 가능 필드 업데이트
        if (email != null && !email.trim().isEmpty()) {
            memberEntity.setEmail(email);
        }
        if (nickname != null && !nickname.trim().isEmpty()) {
            memberEntity.setNickname(nickname);
        }
        memberEntity.setUpdatedAt(java.time.LocalDateTime.now());

        // 저장
        MemberEntity savedEntity = memberRepository.save(memberEntity);

        // Entity -> DTO 변환
        Member member = getMember(savedEntity.getId());
        commonResponse.setResponse(member);

        return commonResponse;
    }

    /**
     * 관리자용 회원 탈퇴 (슈퍼관리자만 가능)
     * @param memberId
     * @return
     */
    public CommonResponse<Void> deleteMemberByAdmin(Long memberId) {

        CommonResponse<Void> commonResponse = new CommonResponse<>();

        // 회원 조회
        Optional<MemberEntity> memberOpt = memberRepository.findById(memberId);
        if (memberOpt.isEmpty()) {
            commonResponse.setResponeInfo(ResponseEnum.MEMBER_NOT_FOUND);
            return commonResponse;
        }

        MemberEntity memberEntity = memberOpt.get();

        // 회원 권한 삭제 (cascade로 자동 삭제되지만 명시적으로)
        memberRoleRepository.deleteAll(memberEntity.getMemberRoles());

        // 회원 삭제
        memberRepository.delete(memberEntity);

        // 해당 회원의 세션 삭제 (Redis에서 모든 세션을 확인하여 삭제)
        // 참고: 실제 구현에서는 Redis에서 해당 회원의 세션을 찾아 삭제하는 로직이 필요할 수 있습니다.
        // 현재는 회원이 삭제되면 다음 요청 시 세션이 자동으로 무효화됩니다.

        return commonResponse;
    }

    /**
     * 회원 목록 조회 (페이징, 검색)
     * @param request
     * @return
     */
    public CommonResponse<MemberListResponseDto> getMemberList(MemberListRequestDto request) {

        CommonResponse<MemberListResponseDto> commonResponse = new CommonResponse<>();

        // 페이징 설정 (최신순 정렬)
        Pageable pageable = PageRequest.of(
                request.getPageNumber(),
                request.getPageSize(),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // 검색 조건 생성
        Specification<MemberEntity> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 유저아이디 검색 (LIKE %검색어%)
            if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        root.get("username"),
                        "%" + request.getUsername() + "%"
                ));
            }

            // 이메일 검색 (LIKE %검색어%)
            if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        root.get("email"),
                        "%" + request.getEmail() + "%"
                ));
            }

            // 닉네임 검색 (LIKE %검색어%)
            if (request.getNickname() != null && !request.getNickname().trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        root.get("nickname"),
                        "%" + request.getNickname() + "%"
                ));
            }

            // 가입일자 범위 검색
            if (request.getCreatedAtStart() != null && !request.getCreatedAtStart().trim().isEmpty()) {
                LocalDateTime startDateTime = LocalDate.parse(request.getCreatedAtStart(), DateTimeFormatter.ISO_DATE).atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            }
            if (request.getCreatedAtEnd() != null && !request.getCreatedAtEnd().trim().isEmpty()) {
                LocalDateTime endDateTime = LocalDate.parse(request.getCreatedAtEnd(), DateTimeFormatter.ISO_DATE).atTime(23, 59, 59);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
            }

            // 승인 상태 검색
            if (request.getApprovalStatus() != null && !request.getApprovalStatus().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("approvalStatus"), request.getApprovalStatus()));
            }

            // 권한으로 검색 (JOIN 필요)
            if (request.getRoleName() != null && !request.getRoleName().trim().isEmpty()) {
                var memberRolesJoin = root.join("memberRoles");
                var roleJoin = memberRolesJoin.join("role");
                predicates.add(criteriaBuilder.equal(roleJoin.get("roleName"), request.getRoleName()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 회원 목록 조회
        Page<MemberEntity> memberPage = memberRepository.findAll(spec, pageable);

        // 응답 DTO 생성
        MemberListResponseDto responseDto = new MemberListResponseDto();
        responseDto.setTotalCount(memberPage.getTotalElements());
        responseDto.setPageSize(request.getPageSize());
        responseDto.setPageNumber(request.getPageNumber());
        responseDto.setTotalPages(memberPage.getTotalPages());

        // 회원 정보 변환 (권한 정보 포함)
        List<MemberListResponseDto.MemberDto> memberDtoList = new ArrayList<>();
        for (MemberEntity entity : memberPage.getContent()) {
            MemberListResponseDto.MemberDto memberDto = new MemberListResponseDto.MemberDto();
            memberDto.setId(entity.getId());
            memberDto.setUsername(entity.getUsername());
            memberDto.setEmail(entity.getEmail());
            memberDto.setNickname(entity.getNickname());
            memberDto.setApprovalStatus(entity.getApprovalStatus());
            if (entity.getCreatedAt() != null) {
                memberDto.setCreatedAt(entity.getCreatedAt().toString());
            }
            if (entity.getUpdatedAt() != null) {
                memberDto.setUpdatedAt(entity.getUpdatedAt().toString());
            }

            // 권한 정보 조회
            CommonResponse<List<String>> roleListRes = roleService.getMemberRoles(entity.getId());
            if (roleListRes != null && roleListRes.getResponse() != null) {
                memberDto.setRoles(roleListRes.getResponse());
            } else {
                memberDto.setRoles(new ArrayList<>());
            }

            memberDtoList.add(memberDto);
        }

        responseDto.setMembers(memberDtoList);
        commonResponse.setResponse(responseDto);

        return commonResponse;
    }


    /**
     * 이메일로 회원 UserName 찾기
     * @param email
     * @return
     */
    public List<String> findUsernamesByEmail(String email) {

        return memberRepository.findByEmail(email).stream()
                .map(e -> {
                    return e.getUsername();
                })
                .toList();
    }

    /**
     * <pre>
     * 비밀번호 초기화 전 단계
     * - username 검색
     * - 이메일 인증
     * </pre>
     * @param username
     * @param email
     * @return
     */
    public CommonResponse<PasswordResetRequestResponseDto> resetPasswordBeforeByEmail(String username, String email) {

        CommonResponse<PasswordResetRequestResponseDto> commonResponse = new CommonResponse<>();

        // 회원 조회
        MemberEntity memberEntity = memberRepository.findByUsername(username);
        if (memberEntity == null) {
            commonResponse.setResponeInfo(ResponseEnum.MEMBER_NOT_FOUND);
            return commonResponse;
        }

        // 이메일 일치 확인
        if (!email.equals(memberEntity.getEmail())) {
            commonResponse.setResponeInfo(ResponseEnum.MEMBER_NOT_FOUND);
            return commonResponse;
        }

        // 인증코드 생성
        String verificationCode = generateVerificationCode();

        // 인증코드 메일 전송
        String subject = "비밀번호 재설정 인증코드";
        String content = buildVerificationEmailContent(verificationCode);
        boolean isSent = mailService.sendHtmlMail(email, subject, content);

        if (!isSent) {
            commonResponse.setResponeInfo(ResponseEnum.MAIL_SEND_ERR);
            return commonResponse;
        }

        // Redis에 인증코드 저장
        String redisKey = buildRedisKey(email, username);
        redisComponent.saveValueWithTtl(redisKey, verificationCode, PASSWORD_RESET_TTL_SECONDS);

        // 만료 시간 계산
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(PASSWORD_RESET_TTL_SECONDS);

        // 응답 DTO 생성
        PasswordResetRequestResponseDto responseDto = new PasswordResetRequestResponseDto();
        responseDto.setEmail(email);
        responseDto.setExpiresInSeconds(PASSWORD_RESET_TTL_SECONDS);
        responseDto.setExpiresAt(expiresAt.toString());

        // 응답 메시지 설정
        commonResponse.setMessage(email + " 주소로 인증코드를 전송했습니다. 이메일을 확인해주세요.");
        commonResponse.setResponse(responseDto);

        return commonResponse;
    }

    /**
     * <pre>
     * 비밀번호 초기화 후 단계
     * - 인증코드 검증
     * - 임시 비밀번호 생성 및 설정
     * </pre>
     * @param username
     * @param email
     * @param verificationCode
     * @return
     */
    public CommonResponse<PasswordResetResponseDto> resetPasswordAfterByEmail(String username, String email, String verificationCode) {

        CommonResponse<PasswordResetResponseDto> commonResponse = new CommonResponse<>();

        // 회원 조회
        MemberEntity memberEntity = memberRepository.findByUsername(username);
        if (memberEntity == null) {
            commonResponse.setResponeInfo(ResponseEnum.MEMBER_NOT_FOUND);
            return commonResponse;
        }

        // 이메일 일치 확인
        if (!email.equals(memberEntity.getEmail())) {
            commonResponse.setResponeInfo(ResponseEnum.MEMBER_NOT_FOUND);
            return commonResponse;
        }

        // Redis에서 인증코드 조회
        String redisKey = buildRedisKey(email, username);
        String savedCode = redisComponent.getValue(redisKey);

        // 인증코드 만료 확인
        if (savedCode == null || savedCode.isEmpty()) {
            commonResponse.setResponeInfo(ResponseEnum.VERIFICATION_CODE_EXPIRED);
            return commonResponse;
        }

        // 인증코드 일치 확인
        if (!verificationCode.equals(savedCode)) {
            commonResponse.setResponeInfo(ResponseEnum.VERIFICATION_CODE_NOT_MATCH);
            return commonResponse;
        }

        // 임시 비밀번호 생성
        String temporaryPassword = generateTemporaryPassword();

        // 비밀번호 암호화 및 저장
        String encryptedPassword = passwordEncoder.encode(temporaryPassword);
        memberEntity.setPassword(encryptedPassword);
        memberEntity.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(memberEntity);

        // Redis에서 인증코드 삭제
        redisComponent.deleteKey(redisKey);

        // 응답 DTO 생성
        PasswordResetResponseDto responseDto = new PasswordResetResponseDto();
        responseDto.setUsername(username);
        responseDto.setTemporaryPassword(temporaryPassword);
        commonResponse.setResponse(responseDto);

        return commonResponse;
    }

    /**
     * 6자리 인증코드 생성
     * @return 인증코드
     */
    private String generateVerificationCode() {
        int code = (int)(Math.random() * (VERIFICATION_CODE_MAX - VERIFICATION_CODE_MIN + 1)) + VERIFICATION_CODE_MIN;
        return String.valueOf(code);
    }

    /**
     * 임시 비밀번호 생성 (영문 대소문자, 숫자, 특수문자 조합)
     * @return 임시 비밀번호
     */
    private String generateTemporaryPassword() {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String specialChars = "!@#$%^&*";
        String allChars = upperCase + lowerCase + digits + specialChars;

        StringBuilder password = new StringBuilder();

        // 각 종류별로 최소 1개씩 포함
        password.append(upperCase.charAt((int)(Math.random() * upperCase.length())));
        password.append(lowerCase.charAt((int)(Math.random() * lowerCase.length())));
        password.append(digits.charAt((int)(Math.random() * digits.length())));
        password.append(specialChars.charAt((int)(Math.random() * specialChars.length())));

        // 나머지 자리 랜덤 채우기
        for (int i = 4; i < TEMPORARY_PASSWORD_LENGTH; i++) {
            password.append(allChars.charAt((int)(Math.random() * allChars.length())));
        }

        // 문자열 섞기
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = (int)(Math.random() * (i + 1));
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    /**
     * 비밀번호 재설정 인증 메일 내용 생성
     * @param verificationCode
     * @return 메일 내용
     */
    private String buildVerificationEmailContent(String verificationCode) {
        String validityMessage;
        if (PASSWORD_RESET_TTL_SECONDS < 60) {
            validityMessage = "인증코드는 " + PASSWORD_RESET_TTL_SECONDS + "초간 유효합니다.";
        } else {
            validityMessage = "인증코드는 " + PASSWORD_RESET_TTL_MINUTES + "분간 유효합니다.";
        }

        return "<html><body>" +
                "<h2>비밀번호 재설정 인증코드</h2>" +
                "<p>아래 인증코드를 입력하여 비밀번호 재설정을 진행해주세요.</p>" +
                "<h3>" + verificationCode + "</h3>" +
                "<p>" + validityMessage + "</p>" +
                "</body></html>";
    }

    /**
     * Redis 키 생성
     * @param email
     * @param username
     * @return Redis 키
     */
    private String buildRedisKey(String email, String username) {
        return email + ":" + username;
    }

}
