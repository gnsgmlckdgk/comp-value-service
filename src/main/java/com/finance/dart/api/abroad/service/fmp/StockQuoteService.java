package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.quote.StockQuoteReqDto;
import com.finance.dart.api.abroad.dto.fmp.quote.StockQuoteResDto;
import com.finance.dart.api.abroad.dto.fmp.quote.StockQuoteShortReqDto;
import com.finance.dart.api.abroad.dto.fmp.quote.StockQuoteShortResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class StockQuoteService {

    private final FmpClientComponent fmpClientComponent;

    /**
     * 주식 시세 조회
     * @param reqDto
     * @return
     */
    public List<StockQuoteResDto> findStockQuote(StockQuoteReqDto reqDto) {

        List<StockQuoteResDto> response = fmpClientComponent.sendGet(
                FmpApiList.StockQuote,
                reqDto,
                new ParameterizedTypeReference<>() {}
        );

        return response;
    }


    /**
     * 주식 시세 조회(간소화 버전)
     * @param reqDto
     * @return
     */
    public List<StockQuoteShortResDto> findStockQuoteShort(StockQuoteShortReqDto reqDto) {

         List<StockQuoteShortResDto> response = fmpClientComponent.sendGet(
                 FmpApiList.StockQuoteShort,
                 reqDto,
                 new ParameterizedTypeReference<>() {}
         );

         return response;
    }

}
