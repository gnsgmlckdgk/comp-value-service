package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.stocknews.StockNewsReqDto;
import com.finance.dart.api.abroad.dto.fmp.stocknews.StockNewsResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class StockNewsService {

    private final FmpClientComponent fmpClientComponent;

    /**
     * 종목별 최신 뉴스 조회
     */
    public List<StockNewsResDto> findStockNews(StockNewsReqDto reqDto) {
        log.info("종목 뉴스 조회 요청 - tickers: {}, limit: {}", reqDto.getTickers(), reqDto.getLimit());
        List<StockNewsResDto> result = fmpClientComponent.sendGet(
                FmpApiList.StockNews,
                reqDto,
                new ParameterizedTypeReference<>() {}
        );
        log.info("종목 뉴스 조회 결과 - {}건 (첫 기사 symbol: {})",
                result != null ? result.size() : 0,
                result != null && !result.isEmpty() ? result.get(0).getSymbol() : "N/A");
        return result;
    }
}
