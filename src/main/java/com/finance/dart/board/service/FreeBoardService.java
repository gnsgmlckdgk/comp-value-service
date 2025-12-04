package com.finance.dart.board.service;

import com.finance.dart.board.dto.FreeBoardDto;
import com.finance.dart.board.entity.FreeBoard;
import com.finance.dart.board.dto.FreeBoardListResponseDto;
import com.finance.dart.board.repository.FreeBoardRepository;
import com.finance.dart.member.dto.Member;
import com.finance.dart.member.entity.MemberEntity;
import com.finance.dart.member.service.MemberService;
import com.finance.dart.member.service.SessionService;
import com.finance.dart.member.enums.Role;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class FreeBoardService {

    private final FreeBoardRepository freeBoardRepository;
    private final MemberService memberService;
    private final SessionService sessionService;

    /**
     * Entity -> DTO 변환
     */
    private FreeBoardDto convertToDto(FreeBoard board) {
        FreeBoardDto dto = new FreeBoardDto();
        dto.setId(board.getId());
        dto.setTitle(board.getTitle());
        dto.setContent(board.getContent());
        dto.setViewCount(board.getViewCount());

        // 작성자 정보
        MemberEntity member = board.getMember();
        if (member != null) {
            dto.setMemberId(member.getId());
            dto.setMemberUsername(member.getUsername());
            dto.setMemberNickname(member.getNickname());
        }

        // 날짜 포맷팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dto.setCreatedAt(board.getCreatedAt().format(formatter));
        dto.setUpdatedAt(board.getUpdatedAt().format(formatter));

        return dto;
    }

    /**
     * 게시글 생성
     */
    public FreeBoardDto createBoard(HttpServletRequest request, FreeBoardDto boardDto) {
        // 로그인 회원 정보
        Member member = memberService.getLoginMember(request);
        if (member == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        // 최소한의 정보만 가진 MemberEntity 구성 (id, username, nickname 정도만 사용)
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(member.getId());
        memberEntity.setUsername(member.getUsername());
        memberEntity.setNickname(member.getNickname());

        // DTO -> Entity 변환
        FreeBoard board = new FreeBoard();
        board.setTitle(boardDto.getTitle());
        board.setContent(boardDto.getContent());
        board.setMember(memberEntity);
        board.setCreatedAt(LocalDateTime.now());
        board.setUpdatedAt(LocalDateTime.now());

        FreeBoard savedBoard = freeBoardRepository.save(board);

        // Entity -> DTO 변환하여 반환
        return convertToDto(savedBoard);
    }

    /**
     * ID로 게시글 조회 (조회수 증가)
     */
    @Transactional
    public FreeBoardDto getBoardById(Long id) {
        Optional<FreeBoard> boardOpt = freeBoardRepository.findById(id);
        FreeBoard board = boardOpt.orElseThrow(() ->
                new RuntimeException("Board not found with id: " + id));

        // 조회수 증가
        board.setViewCount(board.getViewCount() + 1);
        freeBoardRepository.save(board);

        return convertToDto(board);
    }

    /**
     * 모든 게시글 조회 (페이징, 검색)
     */
    @Transactional(readOnly = true)
    public FreeBoardListResponseDto getAllBoards(Pageable pageable, String search, String sgubun) {

        Page<FreeBoard> page;

        // 검색 값 투입
        if("1".equals(sgubun)) {          // 제목으로 검색
            page = freeBoardRepository.findByTitleContaining(search, pageable);
        } else if("2".equals(sgubun)) {   // 작성자로 검색
            page = freeBoardRepository.findByMember_NicknameContaining(search, pageable);
        } else if("3".equals(sgubun)) {   // 내용으로 검색
            page = freeBoardRepository.findByContentContaining(search, pageable);
        } else if("4".equals(sgubun)) {   // 제목, 내용으로 검색
            page = freeBoardRepository.findByTitleContainingOrContentContaining(search, search, pageable);
        } else {    // 전체 조회
            page = freeBoardRepository.findAll(pageable);
        }

        // Entity -> DTO 변환
        List<FreeBoardDto> boards = page.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        long totalElements = page.getTotalElements();

        FreeBoardListResponseDto response = new FreeBoardListResponseDto();
        response.setData(boards);
        response.setTotal(totalElements);

        return response;
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public FreeBoardDto updateBoard(HttpServletRequest request, Long id, FreeBoardDto boardDto) {
        Optional<FreeBoard> boardOpt = freeBoardRepository.findById(id);
        FreeBoard board = boardOpt.orElseThrow(() ->
                new RuntimeException("Board not found with id: " + id));

        // 로그인 회원 정보
        Member loginMember = memberService.getLoginMember(request);

        // 권한 체크
        validateUpdatePermission(request, loginMember, board);

        // 수정
        board.setTitle(boardDto.getTitle());
        board.setContent(boardDto.getContent());
        board.setUpdatedAt(LocalDateTime.now());

        FreeBoard savedBoard = freeBoardRepository.save(board);

        return convertToDto(savedBoard);
    }

    /**
     * 게시글 삭제
     */
    public void deleteBoard(HttpServletRequest request, Long id) {
        Optional<FreeBoard> boardOpt = freeBoardRepository.findById(id);
        FreeBoard board = boardOpt.orElseThrow(() ->
                new RuntimeException("Board not found with id: " + id));

        // 로그인 회원 정보
        Member loginMember = memberService.getLoginMember(request);

        // 권한 체크
        validateDeletePermission(request, loginMember, board);

        freeBoardRepository.delete(board);
    }

    /**
     * 수정 권한 체크
     * 슈퍼관리자, 관리자 : 자기 게시글만 수정 가능
     * 그 외 : 자기 게시글만 수정 가능
     */
    private void validateUpdatePermission(HttpServletRequest request, Member loginMember, FreeBoard board) {

        if (loginMember == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        Long loginMemberId = loginMember.getId();
        Long writerId = board.getMember().getId();

        // 본인 글인지 체크
        if (!loginMemberId.equals(writerId)) {
            throw new RuntimeException("해당 게시글을 수정할 권한이 없습니다.");
        }
    }

    /**
     * 삭제 권한 체크
     * 슈퍼관리자, 관리자 : 모든 게시글 삭제 가능
     * 그 외 : 자기 게시글만 삭제 가능
     */
    private void validateDeletePermission(HttpServletRequest request, Member loginMember, FreeBoard board) {

        if (loginMember == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        Long loginMemberId = loginMember.getId();
        Long writerId = board.getMember().getId();

        // SUPER_ADMIN 또는 ADMIN 권한이면 모두 삭제 가능
        boolean isSuperAdmin = sessionService.hasRole(request, Role.SUPER_ADMIN.getRoleName());
        boolean isAdmin = sessionService.hasRole(request, Role.ADMIN.getRoleName());

        if (isSuperAdmin || isAdmin) {
            return;
        }

        // 그 외 권한은 본인 글만 삭제 가능
        if (!loginMemberId.equals(writerId)) {
            throw new RuntimeException("해당 게시글을 삭제할 권한이 없습니다.");
        }
    }


}
