package com.finance.dart.api.domestic.service.schedule;


import com.finance.dart.api.domestic.component.StockValueComponent;
import com.finance.dart.api.domestic.dto.CorpCodeDTO;
import com.finance.dart.api.domestic.dto.CorpCodeResDTO;
import com.finance.dart.api.domestic.dto.StockValueResultDTO;
import com.finance.dart.api.common.entity.StockValuationResultEntity;
import com.finance.dart.api.domestic.service.CalCompanyStockPerValueService;
import com.finance.dart.api.domestic.service.CorpCodeService;
import com.finance.dart.common.util.DateUtil;
import com.finance.dart.common.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 가치계산 스케줄러 처리 컴포넌트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CalCompanyStockPerValueTotalWorker {

    private final CorpCodeService corpCodeService;
    private final CalCompanyStockPerValueService calCompanyStockPerValueService;
    private final StockValueComponent stockValueComponent;


    @Transactional
    public void process() {
        //@ 기업목록 조회
        CorpCodeResDTO compResDto = corpCodeService.getCorpCode(true);
        if(compResDto == null || compResDto.getList() == null) return;

        List<CorpCodeDTO> compList = compResDto.getList();
        int compCount = compList.size();
        if(log.isDebugEnabled()) log.info("[가치계산 스케줄러] 상장기업 수 : {}", compCount);

        for (int i = 0; i < compCount; i++) {

            CorpCodeDTO comp = compList.get(i);
            if(log.isInfoEnabled()) log.info("[가치계산 스케줄러] {} 검색중...({}/{}) [{}]", comp.getCorpName(), (i+1), compCount, DateUtil.getToday());

            try {
                // 기업가치 계산
                exploreFairyValueComp(comp);

                // 1초 대기
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                log.error("[가치계산 스케줄러] Interrupted during sleep", e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                log.error("[가치계산 스케줄러] Error processing data: {}", comp, e);
            }
        }

        //@ 이전 기록 삭제
        String yesterDay = DateUtil.getOffsetDate(-1, "yyyyMMdd");
        long deleteCnt = stockValueComponent.deleteAllBeforeOrOn(yesterDay);
        if(log.isDebugEnabled()) log.debug("[가치계산 스케줄러] 이전 데이터({} 이전) {}건 삭제됨", yesterDay, deleteCnt);
    }

    /**
     * 가입가치 계산 및 정보 저장
     * @param comp
     */
    private void exploreFairyValueComp(CorpCodeDTO comp) {

        String year = DateUtil.getToday("yyyy");
        try {
            //@ 기업가치 계산
            StockValueResultDTO stockValueResultDTO =
                    calCompanyStockPerValueService.calPerValue(year, comp.getCorpCode(), comp.getCorpName());
            if(log.isDebugEnabled()) log.debug("[가치계산 스케줄러] 기업가치 계산 결과 [{}] = {}", comp.getCorpName(), stockValueResultDTO);

            //@ 결과 DB 저장
            StockValuationResultEntity param = setParam(stockValueResultDTO);
            if(log.isDebugEnabled()) log.debug("[가치계산 스케줄러] 조회값 저장 [{}] = {}", comp.getCorpName(), param);
            stockValueComponent.createCompStockValue(param);

        } catch (Exception e) {
            log.error("[가지계산 스케줄러] 오류 = [{}]", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * DB 데이터 조립
     * @param stockValueResultDTO
     * @return
     */
    private StockValuationResultEntity setParam(StockValueResultDTO stockValueResultDTO) {

        StockValuationResultEntity param = new StockValuationResultEntity();

        param.setBaseDate(DateUtil.getToday("yyyyMMdd"));

        param.setCompanyName(StringUtil.defaultString(stockValueResultDTO.get기업명()));
        param.setCompanyCode(StringUtil.defaultString(stockValueResultDTO.get기업코드()));
        param.setStockCode(StringUtil.defaultString(stockValueResultDTO.get주식코드()));

        long 주당가치 = StringUtil.defaultLong(stockValueResultDTO.get주당가치());
        long 현재가격 = StringUtil.defaultLong(stockValueResultDTO.get현재가격());

        param.setPerShareValue(주당가치);
        param.setCurrentPrice(현재가격);
        param.setDifference(주당가치-현재가격);

        param.setResultMessage(StringUtil.cutStringDelete(stockValueResultDTO.get결과메시지(), 200, "UTF-8"));
        param.setNote("");

        return param;
    }

}
