package com.finance.dart.board.service;

import com.finance.dart.api.abroad.dto.fmp.company.CompanyProfileDataResDto;
import com.finance.dart.api.abroad.service.fmp.CompanyProfileSearchService;
import com.finance.dart.board.dto.TranRecordCurValueResDto;
import com.finance.dart.board.dto.TranRecordDto;
import com.finance.dart.board.entity.TranRecordEntity;
import com.finance.dart.board.repository.TranRecordRepository;
import com.finance.dart.common.util.ConvertUtil;
import com.finance.dart.common.util.DateUtil;
import com.finance.dart.common.util.StringUtil;
import com.finance.dart.member.dto.Member;
import com.finance.dart.member.entity.MemberEntity;
import com.finance.dart.member.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@Slf4j
@AllArgsConstructor
@Service
public class TranRecordService {

    private final TranRecordRepository tranRecordRepository;

    private final MemberService memberService;
    private final CompanyProfileSearchService companyProfileSearchService;


    /**
     * 거래기록 등록
     * @param tranRecordEntity
     * @return
     */
    public TranRecordEntity regiTranRecord(HttpServletRequest request, TranRecordEntity tranRecordEntity) {

        // 로그인 회원 정보
        Member member = memberService.getLoginMember(request);
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setId(member.getId());

        // 데이터 저장
        tranRecordEntity.setMember(memberEntity);
        TranRecordEntity resultEntity = tranRecordRepository.save(tranRecordEntity);

        return resultEntity;
    }

    /**
     * 거래기록 목록 조회
     * @param request
     * @return
     */
    public List<TranRecordDto> getTranRecordList(HttpServletRequest request) {

        List<TranRecordDto> tranRecordDtoList = new LinkedList<>();

        // 로그인 회원정보
        Member member = memberService.getLoginMember(request);
        Long memberId = member.getId();

        // 거래기록 조회
        List<TranRecordEntity> tranRecordEntityList = tranRecordRepository.findByMember_Id(memberId);

        for(TranRecordEntity tranRecordEntity : tranRecordEntityList) {

            TranRecordDto tranRecordDto = ConvertUtil.parseObject(tranRecordEntity, TranRecordDto.class);

            // 현재가 갱신
            TranRecordCurValueResDto curValue = getCurValue(tranRecordDto.getSymbol());
            tranRecordDto.setCurrentPrice(curValue == null ? 0 : curValue.getCurrentPrice());

            tranRecordDtoList.add(tranRecordDto);
        }

        return tranRecordDtoList;
    }

    /**
     * 현재가격 조회(단건)
     * @param symbol
     * @return
     */
    public TranRecordCurValueResDto getCurValue(String symbol) {

        if(!StringUtil.isStringEmpty(symbol)) {
            Set<String> symbolSet = new LinkedHashSet<>();
            symbolSet.add(symbol);

            List<TranRecordCurValueResDto> values = getCurValues(symbolSet);
            if(values == null || values.isEmpty()) return null;

            return values.get(0);
        }

        return null;
    }

    /**
     * 현재가격 조회(다건)
     * @param symbolSet
     * @return
     */
    public List<TranRecordCurValueResDto> getCurValues(Set<String> symbolSet) {

        if (symbolSet == null || symbolSet.isEmpty()) {
            return null;
        }

        List<TranRecordCurValueResDto> resultList = new LinkedList<>();
        List<String> symbolList = symbolSet.stream().toList();

        for(String symbol : symbolList) {

            TranRecordCurValueResDto tranRecordCurValueResDto = new TranRecordCurValueResDto();

            List<CompanyProfileDataResDto> profiles = companyProfileSearchService.findProfileListBySymbol(symbol);

            if(profiles == null || profiles.size() == 0) {
                tranRecordCurValueResDto.setSymbol(symbol);

            } else {
                CompanyProfileDataResDto profile = profiles.get(0);
                tranRecordCurValueResDto.setSymbol(profile.getSymbol());
                tranRecordCurValueResDto.setCurrentPrice(profile.getPrice());

            }

            tranRecordCurValueResDto.setUpdatedAt(DateUtil.getToday("yyyy-MM-dd HH:mm:ss"));
            resultList.add(tranRecordCurValueResDto);
        }

        return resultList;
    }

}
