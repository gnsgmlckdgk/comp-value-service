package com.finance.dart.board.service;

import com.finance.dart.api.abroad.dto.fmp.forexquote.ForexQuoteReqDto;
import com.finance.dart.api.abroad.dto.fmp.forexquote.ForexQuoteResDto;
import com.finance.dart.api.abroad.dto.fmp.quote.AfterTradeReqDto;
import com.finance.dart.api.abroad.dto.fmp.quote.AfterTradeResDto;
import com.finance.dart.api.abroad.service.fmp.AfterTradeService;
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
    private final AfterTradeService afterTradeService;                          // 애프터마켓 시세 조회 서비스


    /**
     * 거래기록 등록
     * @param tranRecordDto
     * @return
     */
    public TranRecordDto regiTranRecord(HttpServletRequest request, TranRecordDto tranRecordDto) {

        // 로그인 회원 정보
        MemberEntity memberEntity = getMemberEntity(request);

        // DTO -> Entity 변환
        TranRecordEntity tranRecordEntity = ConvertUtil.parseObject(tranRecordDto, TranRecordEntity.class);
        tranRecordEntity.setMember(memberEntity);

        // 데이터 저장
        TranRecordEntity savedEntity = tranRecordRepository.save(tranRecordEntity);

        // Entity -> DTO 변환하여 반환
        return ConvertUtil.parseObject(savedEntity, TranRecordDto.class);
    }

    /**
     * 거래기록 수정
     * @param request
     * @param updateDto
     * @return
     */
    public TranRecordDto modiTranRecord(HttpServletRequest request, TranRecordDto updateDto) {

        // 로그인 회원 정보
        MemberEntity memberEntity = getMemberEntity(request);

        // 데이터 조회
        Optional<TranRecordEntity> dataOpt = tranRecordRepository.findById(updateDto.getId());
        if(dataOpt.isEmpty()) return null;

        // 데이터 수정
        TranRecordEntity data = dataOpt.get();
        data.setMember(memberEntity);

        if(updateDto.getSymbol() != null) data.setSymbol(updateDto.getSymbol());
        if(updateDto.getCompanyName() != null) data.setCompanyName(updateDto.getCompanyName());
        if(updateDto.getBuyDate() != null) data.setBuyDate(updateDto.getBuyDate());
        if(updateDto.getBuyPrice() != null) data.setBuyPrice(updateDto.getBuyPrice());
        if(updateDto.getTotalBuyAmount() != null) data.setTotalBuyAmount(updateDto.getTotalBuyAmount());
        if(updateDto.getTargetPrice() != null) data.setTargetPrice(updateDto.getTargetPrice());
        if(updateDto.getRmk() != null) data.setRmk(updateDto.getRmk());
        data.setUpdatedAt(LocalDateTime.now());

        TranRecordEntity savedEntity = tranRecordRepository.save(data);

        // Entity -> DTO 변환하여 반환
        return ConvertUtil.parseObject(savedEntity, TranRecordDto.class);
    }

    /**
     * 거래기록 삭제
     * @param deleteDto
     * @return
     */
    public void delTranRecord(TranRecordDto deleteDto) {

        if (deleteDto.getId() != null) {
            tranRecordRepository.deleteById(deleteDto.getId());
        }
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

        // 티커 분류
        Map<String, Double> tickerValueMap = new HashMap<>();
        for(TranRecordEntity tranRecordEntity : tranRecordEntityList) {
            tickerValueMap.put(tranRecordEntity.getSymbol(), 0.0);
        }

        // 현재가 조회
        for(String ticker : tickerValueMap.keySet()) {
            TranRecordCurValueResDto curValue = getCurValue(ticker);
            tickerValueMap.put(ticker, (curValue == null || curValue.getCurrentPrice() == null) ?
                    0 : curValue.getCurrentPrice());
        }

        // 현재가 세팅
        for(TranRecordEntity tranRecordEntity : tranRecordEntityList) {
            TranRecordDto tranRecordDto = ConvertUtil.parseObject(tranRecordEntity, TranRecordDto.class);
            tranRecordDto.setCurrentPrice(tickerValueMap.get(tranRecordDto.getSymbol()));
            tranRecordDtoList.add(tranRecordDto);
        }


        // OLD
//        for(TranRecordEntity tranRecordEntity : tranRecordEntityList) {
//
//            TranRecordDto tranRecordDto = ConvertUtil.parseObject(tranRecordEntity, TranRecordDto.class);
//
//            // 현재가 갱신
//            TranRecordCurValueResDto curValue = getCurValue(tranRecordDto.getSymbol());
//            tranRecordDto.setCurrentPrice((curValue == null || curValue.getCurrentPrice() == null) ?
//                    0 : curValue.getCurrentPrice());
//
//            tranRecordDtoList.add(tranRecordDto);
//        }

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

            // 정규장
            List<ForexQuoteResDto> forexQuoteResDtoList = forexQuoteService.findForexQuote(new ForexQuoteReqDto(symbol));
            // 애프터장
            List<AfterTradeResDto> afterTradeResDtoList = afterTradeService.findAfterTrade(new AfterTradeReqDto(symbol));

            if(forexQuoteResDtoList == null && forexQuoteResDtoList.size() == 0) tranRecordCurValueResDto.setSymbol(symbol);
            else {
                ForexQuoteResDto forexQuoteResDto = forexQuoteResDtoList.get(0);
                tranRecordCurValueResDto.setCurrentPrice(forexQuoteResDto.getPrice());

                if(afterTradeResDtoList != null || afterTradeResDtoList.size() > 0) {

                    AfterTradeResDto afterTradeResDto = afterTradeResDtoList.get(0);

                    long quoteTimestamp = forexQuoteResDto.getTimestamp();      // 정규장
                    long afterTradeTimestamp = afterTradeResDto.getTimestamp(); // 애프터마켓

                    if(afterTradeTimestamp > quoteTimestamp) {  // 애프터마켓이 더 최신
                        tranRecordCurValueResDto.setCurrentPrice(afterTradeResDto.getPrice());
                    }
                }

            }

            // 이전 버전(2025.10.30)
//            List<CompanyProfileDataResDto> profiles = companyProfileSearchService.findProfileListBySymbol(symbol);
//
//            if(profiles == null || profiles.size() == 0) {
//                tranRecordCurValueResDto.setSymbol(symbol);
//
//            } else {
//                CompanyProfileDataResDto profile = profiles.get(0);
//                tranRecordCurValueResDto.setSymbol(profile.getSymbol());
//                tranRecordCurValueResDto.setCurrentPrice(profile.getPrice());
//            }

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
