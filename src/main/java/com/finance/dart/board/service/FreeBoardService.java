package com.finance.dart.board.service;

import com.finance.dart.board.dto.FreeBoardDto;
import com.finance.dart.board.entity.FreeBoard;
import com.finance.dart.board.dto.FreeBoardListResponseDto;
import com.finance.dart.board.repository.FreeBoardRepository;
import com.finance.dart.common.util.ConvertUtil;
import com.finance.dart.member.dto.Member;
import com.finance.dart.member.entity.MemberEntity;
import com.finance.dart.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        MemberEntity memberEntity = ConvertUtil.parseObject(member, MemberEntity.class);

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
    public FreeBoardDto updateBoard(Long id, FreeBoardDto boardDto) {
        Optional<FreeBoard> boardOpt = freeBoardRepository.findById(id);
        FreeBoard board = boardOpt.orElseThrow(() ->
                new RuntimeException("Board not found with id: " + id));

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
    public void deleteBoard(Long id) {
        Optional<FreeBoard> boardOpt = freeBoardRepository.findById(id);
        FreeBoard board = boardOpt.orElseThrow(() ->
                new RuntimeException("Board not found with id: " + id));

        freeBoardRepository.delete(board);
    }


}
