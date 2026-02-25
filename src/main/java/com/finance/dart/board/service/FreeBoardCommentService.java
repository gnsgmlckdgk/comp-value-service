package com.finance.dart.board.service;

import com.finance.dart.board.dto.FreeBoardCommentDto;
import com.finance.dart.board.entity.FreeBoard;
import com.finance.dart.board.entity.FreeBoardComment;
import com.finance.dart.board.repository.FreeBoardCommentRepository;
import com.finance.dart.board.repository.FreeBoardRepository;
import com.finance.dart.common.exception.BizException;
import com.finance.dart.member.dto.Member;
import com.finance.dart.member.entity.MemberEntity;
import com.finance.dart.member.enums.RoleConstants;
import com.finance.dart.member.service.MemberService;
import com.finance.dart.member.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class FreeBoardCommentService {

    private static final int MAX_DEPTH = 1;

    private final FreeBoardCommentRepository commentRepository;
    private final FreeBoardRepository freeBoardRepository;
    private final MemberService memberService;
    private final SessionService sessionService;

    /**
     * 댓글 목록 조회 (계층 구조)
     */
    @Transactional(readOnly = true)
    public List<FreeBoardCommentDto> getComments(Long freeBoardId) {
        List<FreeBoardComment> topLevelComments =
                commentRepository.findByFreeBoardIdAndParentIsNullOrderByCreatedAtAsc(freeBoardId);

        return topLevelComments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 댓글/답글 작성
     */
    @Transactional
    public FreeBoardCommentDto createComment(HttpServletRequest request, Long freeBoardId, FreeBoardCommentDto dto) {
        Member loginMember = memberService.getLoginMember(request);
        if (loginMember == null) {
            throw new BizException("로그인이 필요합니다.");
        }

        FreeBoard board = freeBoardRepository.findById(freeBoardId)
                .orElseThrow(() -> new BizException("게시글을 찾을 수 없습니다."));

        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(loginMember.getId());
        memberEntity.setUsername(loginMember.getUsername());
        memberEntity.setNickname(loginMember.getNickname());

        FreeBoardComment comment = new FreeBoardComment();
        comment.setContent(dto.getContent());
        comment.setFreeBoard(board);
        comment.setMember(memberEntity);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());

        // 답글인 경우
        if (dto.getParentId() != null) {
            FreeBoardComment parent = commentRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new BizException("부모 댓글을 찾을 수 없습니다."));

            if (parent.getDepth() >= MAX_DEPTH) {
                throw new BizException("더 이상 답글을 작성할 수 없습니다.");
            }

            comment.setParent(parent);
            comment.setDepth(parent.getDepth() + 1);
        }

        FreeBoardComment savedComment = commentRepository.save(comment);

        // 댓글 수 동기화
        syncCommentCount(board);

        return convertToDto(savedComment);
    }

    /**
     * 댓글 수정 (본인만)
     */
    @Transactional
    public FreeBoardCommentDto updateComment(HttpServletRequest request, Long commentId, FreeBoardCommentDto dto) {
        Member loginMember = memberService.getLoginMember(request);
        if (loginMember == null) {
            throw new BizException("로그인이 필요합니다.");
        }

        FreeBoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BizException("댓글을 찾을 수 없습니다."));

        // 본인만 수정 가능
        if (!loginMember.getId().equals(comment.getMember().getId())) {
            throw new BizException("해당 댓글을 수정할 권한이 없습니다.");
        }

        comment.setContent(dto.getContent());
        comment.setUpdatedAt(LocalDateTime.now());

        FreeBoardComment savedComment = commentRepository.save(comment);
        return convertToDto(savedComment);
    }

    /**
     * 댓글 삭제 (논리삭제 - 본인 + 관리자)
     */
    @Transactional
    public void deleteComment(HttpServletRequest request, Long commentId) {
        Member loginMember = memberService.getLoginMember(request);
        if (loginMember == null) {
            throw new BizException("로그인이 필요합니다.");
        }

        FreeBoardComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BizException("댓글을 찾을 수 없습니다."));

        // 삭제 권한 체크: SUPER_ADMIN/ADMIN은 모두, 그 외 본인만
        boolean isSuperAdmin = sessionService.hasRole(request, RoleConstants.ROLE_SUPER_ADMIN);
        boolean isAdmin = sessionService.hasRole(request, RoleConstants.ROLE_ADMIN);

        if (!isSuperAdmin && !isAdmin && !loginMember.getId().equals(comment.getMember().getId())) {
            throw new BizException("해당 댓글을 삭제할 권한이 없습니다.");
        }

        comment.setDeleted(true);
        comment.setUpdatedAt(LocalDateTime.now());
        commentRepository.save(comment);

        // 댓글 수 동기화
        syncCommentCount(comment.getFreeBoard());
    }

    /**
     * 댓글 수 동기화
     */
    private void syncCommentCount(FreeBoard board) {
        int count = commentRepository.countByFreeBoardIdAndDeletedFalse(board.getId());
        board.setCommentCount(count);
        freeBoardRepository.save(board);
    }

    /**
     * Entity -> DTO 변환 (재귀)
     */
    private FreeBoardCommentDto convertToDto(FreeBoardComment comment) {
        FreeBoardCommentDto dto = new FreeBoardCommentDto();
        dto.setId(comment.getId());
        dto.setFreeBoardId(comment.getFreeBoard().getId());
        dto.setDepth(comment.getDepth());
        dto.setDeleted(comment.isDeleted());

        if (comment.isDeleted()) {
            dto.setContent("삭제된 댓글입니다.");
        } else {
            dto.setContent(comment.getContent());
        }

        if (comment.getParent() != null) {
            dto.setParentId(comment.getParent().getId());
        }

        // 작성자 정보
        MemberEntity member = comment.getMember();
        if (member != null) {
            dto.setMemberId(member.getId());
            dto.setMemberUsername(member.getUsername());
            dto.setMemberNickname(member.getNickname());
        }

        // 날짜 포맷팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dto.setCreatedAt(comment.getCreatedAt().format(formatter));
        dto.setUpdatedAt(comment.getUpdatedAt().format(formatter));

        // 답글 재귀 변환
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            dto.setReplies(comment.getReplies().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}
