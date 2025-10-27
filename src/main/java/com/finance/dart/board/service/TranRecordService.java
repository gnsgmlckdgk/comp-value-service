package com.finance.dart.board.service;

import com.finance.dart.api.abroad.dto.fmp.company.CompanyProfileDataResDto;
import com.finance.dart.api.abroad.dto.fmp.forexquote.ForexQuoteReqDto;
import com.finance.dart.api.abroad.dto.fmp.forexquote.ForexQuoteResDto;
import com.finance.dart.api.abroad.service.fmp.CompanyProfileSearchService;
import com.finance.dart.api.abroad.service.fmp.ForexQuoteService;
import com.finance.dart.board.dto.TranRecordCurValueResDto;
import com.finance.dart.board.dto.TranRecordDto;
import com.finance.dart.board.dto.TranRecordFxRateResDto;
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

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Service
public class TranRecordService {

    private final TranRecordRepository tranRecordRepository;

    private final MemberService memberService;
    private final CompanyProfileSearchService companyProfileSearchService;      // 기업 프로파일 검색 서비스
    private final ForexQuoteService forexQuoteService;                          // 외환시세 조회 서비스


    /**
     * 거래기록 등록
     * @param tranRecordEntity
     * @return
     */
    public TranRecordEntity regiTranRecord(HttpServletRequest request, TranRecordEntity tranRecordEntity) {

        // 로그인 회원 정보
        MemberEntity memberEntity = getMemberEntity(request);

        // 데이터 저장
        tranRecordEntity.setMember(memberEntity);
        TranRecordEntity resultEntity = tranRecordRepository.save(tranRecordEntity);

        return resultEntity;
    }

    /**
     * 거래기록 수정
     * @param request
     * @param updateEntity
     * @return
     */
    public TranRecordEntity modiTranRecord(HttpServletRequest request, TranRecordEntity updateEntity) {

        // 로그인 회원 정보
        MemberEntity memberEntity = getMemberEntity(request);

        // 데이터 조회
        Optional<TranRecordEntity> dataOpt = tranRecordRepository.findById(updateEntity.getId());
        if(dataOpt.isEmpty()) return null;

        // 데이터 수정
        TranRecordEntity data = dataOpt.get();
        data.setMember(memberEntity);

        if(updateEntity.getSymbol() != null) data.setSymbol(updateEntity.getSymbol());
        if(updateEntity.getCompanyName() != null) data.setCompanyName(updateEntity.getCompanyName());
        if(updateEntity.getBuyDate() != null) data.setBuyDate(updateEntity.getBuyDate());
        if(updateEntity.getBuyPrice() != null) data.setBuyPrice(updateEntity.getBuyPrice());
        if(updateEntity.getTotalBuyAmount() != null) data.setTotalBuyAmount(updateEntity.getTotalBuyAmount());
        if(updateEntity.getTargetPrice() != null) data.setTargetPrice(updateEntity.getTargetPrice());
        if(updateEntity.getRmk() != null) data.setRmk(updateEntity.getRmk());
        data.setUpdatedAt(LocalDateTime.now());

        TranRecordEntity result = tranRecordRepository.save(data);

        return result;
    }

    /**
     * 거래기록 삭제
     * @param deleteEntity
     * @return
     */
    public TranRecordEntity delTranRecord(TranRecordEntity deleteEntity) {

        tranRecordRepository.delete(deleteEntity);

        return deleteEntity;
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
            tranRecordDto.setCurrentPrice((curValue == null || curValue.getCurrentPrice() == null) ?
                    0 : curValue.getCurrentPrice());

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

    /**
     * 환율 조회
     * @param currency
     * @return
     */
    public TranRecordFxRateResDto getFxRate(String currency) {

        TranRecordFxRateResDto result = new TranRecordFxRateResDto();

        ForexQuoteReqDto forexQuoteReqDto = new ForexQuoteReqDto(currency);
        List<ForexQuoteResDto> resList = forexQuoteService.findForexQuote(forexQuoteReqDto);

        if(resList == null || resList.size() == 0) return null;

        ForexQuoteResDto forexQuoteResDto = resList.get(0);

        result.setRate(forexQuoteResDto.getPrice());
        result.setUpdatedAt(DateUtil.getToday("yyyy-MM-dd HH:mm:ss"));

        return result;
    }

    /**
     * 로그인 회원정보 조회
     * @param request
     * @return
     */
    private MemberEntity getMemberEntity(HttpServletRequest request) {
        // 로그인 회원 정보
        Member member = memberService.getLoginMember(request);
        MemberEntity memberEntity = ConvertUtil.parseObject(member, MemberEntity.class);

        return memberEntity;
    }
}
