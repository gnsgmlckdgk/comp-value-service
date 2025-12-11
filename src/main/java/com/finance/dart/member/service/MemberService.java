package com.finance.dart.member.service;

import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.common.service.RedisComponent;
import com.finance.dart.common.util.ConvertUtil;
import com.finance.dart.member.dto.LoginDTO;
import com.finance.dart.member.dto.Member;
import com.finance.dart.member.dto.MemberJoinReqDto;
import com.finance.dart.member.dto.MemberListRequestDto;
import com.finance.dart.member.dto.MemberListResponseDto;
import com.finance.dart.member.entity.MemberEntity;
import com.finance.dart.member.entity.MemberRoleEntity;
import com.finance.dart.member.entity.RoleEntity;
import com.finance.dart.member.enums.Role;
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

    private final SessionService sessionService;
    private final RoleService roleService;
    private final RedisComponent redisComponent;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    private final RoleRepository roleRepository;
    private final MemberRoleRepository memberRoleRepository;


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
        Optional<RoleEntity> optRole = roleRepository.findByRoleName(Role.USER.getRoleName());
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

}
