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
        return fmpClientComponent.sendGet(
                FmpApiList.StockNews,
                reqDto,
                new ParameterizedTypeReference<>() {}
        );
    }
}
