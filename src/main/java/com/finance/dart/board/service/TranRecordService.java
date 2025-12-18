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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@AllArgsConstructor
@Service
public class TranRecordService {

    private final TranRecordRepository tranRecordRepository;

    private final MemberService memberService;
    private final CompanyProfileSearchService companyProfileSearchService;      // 기업 프로파일 검색 서비스
    private final ForexQuoteService forexQuoteService;                          // 외환시세 조회 서비스
    private final AfterTradeService afterTradeService;                          // 애프터마켓 시세 조회 서비스

    // 병렬 처리용 스레드 풀 (최대 10개 동시 실행)
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);


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

        // 티커 분류 (중복 제거)
        Set<String> uniqueTickers = new LinkedHashSet<>();
        for(TranRecordEntity tranRecordEntity : tranRecordEntityList) {
            uniqueTickers.add(tranRecordEntity.getSymbol());
        }

        // 현재가 조회 (병렬 처리)
        List<TranRecordCurValueResDto> curValues = getCurValues(uniqueTickers);

        // 티커별 현재가 맵 구성
        Map<String, Double> tickerValueMap = new HashMap<>();
        if(curValues != null) {
            for(TranRecordCurValueResDto curValue : curValues) {
                tickerValueMap.put(curValue.getSymbol(),
                    (curValue.getCurrentPrice() == null) ? 0.0 : curValue.getCurrentPrice());
            }
        }

        // 현재가 세팅
        for(TranRecordEntity tranRecordEntity : tranRecordEntityList) {
            TranRecordDto tranRecordDto = ConvertUtil.parseObject(tranRecordEntity, TranRecordDto.class);
            tranRecordDto.setCurrentPrice(tickerValueMap.getOrDefault(tranRecordDto.getSymbol(), 0.0));
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
     * 현재가격 조회(다건) - 병렬 처리 버전
     * @param symbolSet
     * @return
     */
    public List<TranRecordCurValueResDto> getCurValues(Set<String> symbolSet) {

        if (symbolSet == null || symbolSet.isEmpty()) {
            return null;
        }

        List<String> symbolList = symbolSet.stream().toList();

        // 각 심볼에 대해 병렬로 API 호출
        List<CompletableFuture<TranRecordCurValueResDto>> futures = symbolList.stream()
                .map(symbol -> CompletableFuture.supplyAsync(() -> {
                    try {
                        return fetchCurrentPriceForSymbol(symbol);
                    } catch (Exception e) {
                        log.error("심볼 {} 가격 조회 중 오류 발생: {}", symbol, e.getMessage());
                        // 오류 발생 시 기본값 반환
                        TranRecordCurValueResDto errorDto = new TranRecordCurValueResDto();
                        errorDto.setSymbol(symbol);
                        errorDto.setCurrentPrice(0.0);
                        errorDto.setUpdatedAt(DateUtil.getToday("yyyy-MM-dd HH:mm:ss"));
                        return errorDto;
                    }
                }, executorService))
                .collect(Collectors.toList());

        // 모든 비동기 작업이 완료될 때까지 대기하고 결과 수집
        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    /**
     * 단일 심볼에 대한 현재가 조회 (병렬 처리용 헬퍼 메서드)
     * @param symbol
     * @return
     */
    private TranRecordCurValueResDto fetchCurrentPriceForSymbol(String symbol) {
        TranRecordCurValueResDto tranRecordCurValueResDto = new TranRecordCurValueResDto();
        tranRecordCurValueResDto.setSymbol(symbol);

        // 정규장과 애프터장 API 호출 (각 심볼에 대해 병렬로 실행됨)
        List<ForexQuoteResDto> forexQuoteResDtoList = forexQuoteService.findForexQuote(new ForexQuoteReqDto(symbol));
        List<AfterTradeResDto> afterTradeResDtoList = afterTradeService.findAfterTrade(new AfterTradeReqDto(symbol));

        // 정규장 데이터 처리
        if(forexQuoteResDtoList == null || forexQuoteResDtoList.isEmpty()) {
            tranRecordCurValueResDto.setCurrentPrice(0.0);
        } else {
            ForexQuoteResDto forexQuoteResDto = forexQuoteResDtoList.get(0);
            tranRecordCurValueResDto.setCurrentPrice(forexQuoteResDto.getPrice());

            // 애프터장 데이터가 있고 더 최신이면 업데이트
            if(afterTradeResDtoList != null && !afterTradeResDtoList.isEmpty()) {
                AfterTradeResDto afterTradeResDto = afterTradeResDtoList.get(0);

                long quoteTimestamp = forexQuoteResDto.getTimestamp();      // 정규장
                long afterTradeTimestamp = afterTradeResDto.getTimestamp(); // 애프터마켓

                if(afterTradeTimestamp > quoteTimestamp) {  // 애프터마켓이 더 최신
                    tranRecordCurValueResDto.setCurrentPrice(afterTradeResDto.getPrice());
                }
            }
        }

        tranRecordCurValueResDto.setUpdatedAt(DateUtil.getToday("yyyy-MM-dd HH:mm:ss"));
        return tranRecordCurValueResDto;
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
