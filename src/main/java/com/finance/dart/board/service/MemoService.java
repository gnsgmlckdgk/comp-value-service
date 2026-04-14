package com.finance.dart.board.service;

import com.finance.dart.board.dto.MemoDto;
import com.finance.dart.board.entity.MemoEntity;
import com.finance.dart.board.repository.MemoRepository;
import com.finance.dart.common.exception.BizException;
import com.finance.dart.common.util.ConvertUtil;
import com.finance.dart.member.dto.Member;
import com.finance.dart.member.entity.MemberEntity;
import com.finance.dart.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class MemoService {

    private final MemoRepository memoRepository;
    private final MemberService memberService;

    /**
     * 메모 목록 조회
     * @param request HTTP 요청 (로그인 회원 식별)
     * @param category 카테고리 필터 (null이면 전체)
     * @return 메모 목록
     */
    public List<MemoDto> getMemoList(HttpServletRequest request, String category) {
        MemberEntity memberEntity = getMemberEntity(request);
        Long memberId = memberEntity.getId();

        List<MemoEntity> entities;
        if (category == null) {
            entities = memoRepository.findByMember_IdOrderByPinnedDescCreatedAtDesc(memberId);
        } else {
            entities = memoRepository.findByMember_IdAndCategoryOrderByPinnedDescCreatedAtDesc(memberId, category);
        }

        return entities.stream()
                .map(entity -> ConvertUtil.parseObject(entity, MemoDto.class))
                .collect(Collectors.toList());
    }

    /**
     * 메모 단건 조회
     * @param request HTTP 요청 (로그인 회원 식별)
     * @param id 메모 ID
     * @return 메모 상세
     */
    public MemoDto getMemo(HttpServletRequest request, Long id) {
        MemberEntity memberEntity = getMemberEntity(request);

        Optional<MemoEntity> dataOpt = memoRepository.findById(id);
        if (dataOpt.isEmpty()) {
            throw new BizException("메모를 찾을 수 없습니다.");
        }

        MemoEntity entity = dataOpt.get();
        if (!entity.getMember().getId().equals(memberEntity.getId())) {
            throw new BizException("접근 권한이 없습니다.");
        }

        return ConvertUtil.parseObject(entity, MemoDto.class);
    }

    /**
     * 메모 생성
     * @param request HTTP 요청 (로그인 회원 식별)
     * @param dto 메모 데이터
     * @return 생성된 메모
     */
    public MemoDto createMemo(HttpServletRequest request, MemoDto dto) {
        MemberEntity memberEntity = getMemberEntity(request);

        MemoEntity entity = ConvertUtil.parseObject(dto, MemoEntity.class);
        entity.setMember(memberEntity);

        MemoEntity savedEntity = memoRepository.save(entity);

        return ConvertUtil.parseObject(savedEntity, MemoDto.class);
    }

    /**
     * 메모 수정
     * @param request HTTP 요청 (로그인 회원 식별)
     * @param dto 수정할 메모 데이터
     * @return 수정된 메모
     */
    public MemoDto updateMemo(HttpServletRequest request, MemoDto dto) {
        MemberEntity memberEntity = getMemberEntity(request);

        Optional<MemoEntity> dataOpt = memoRepository.findById(dto.getId());
        if (dataOpt.isEmpty()) {
            throw new BizException("메모를 찾을 수 없습니다.");
        }

        MemoEntity data = dataOpt.get();
        if (!data.getMember().getId().equals(memberEntity.getId())) {
            throw new BizException("접근 권한이 없습니다.");
        }

        if (dto.getTitle() != null) data.setTitle(dto.getTitle());
        if (dto.getContent() != null) data.setContent(dto.getContent());
        if (dto.getCategory() != null) data.setCategory(dto.getCategory());
        data.setPinned(dto.isPinned());
        data.setUpdatedAt(LocalDateTime.now());

        MemoEntity savedEntity = memoRepository.save(data);

        return ConvertUtil.parseObject(savedEntity, MemoDto.class);
    }

    /**
     * 메모 삭제
     * @param request HTTP 요청 (로그인 회원 식별)
     * @param id 메모 ID
     */
    public void deleteMemo(HttpServletRequest request, Long id) {
        MemberEntity memberEntity = getMemberEntity(request);

        Optional<MemoEntity> dataOpt = memoRepository.findById(id);
        if (dataOpt.isEmpty()) {
            throw new BizException("메모를 찾을 수 없습니다.");
        }

        MemoEntity entity = dataOpt.get();
        if (!entity.getMember().getId().equals(memberEntity.getId())) {
            throw new BizException("접근 권한이 없습니다.");
        }

        memoRepository.deleteById(id);
    }

    /**
     * 카테고리 목록 조회
     * @param request HTTP 요청 (로그인 회원 식별)
     * @return 카테고리 목록
     */
    public List<String> getCategories(HttpServletRequest request) {
        MemberEntity memberEntity = getMemberEntity(request);
        return memoRepository.findDistinctCategoryByMember_Id(memberEntity.getId());
    }

    /**
     * 로그인 회원정보 조회
     * @param request HTTP 요청
     * @return 회원 엔티티
     */
    private MemberEntity getMemberEntity(HttpServletRequest request) {
        Member member = memberService.getLoginMember(request);
        if (member == null) {
            throw new RuntimeException("로그인이 필요합니다.");
        }

        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(member.getId());
        memberEntity.setUsername(member.getUsername());
        memberEntity.setNickname(member.getNickname());

        return memberEntity;
    }
}
