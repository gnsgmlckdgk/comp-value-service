package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.stockscreener.StockScreenerReqDto;
import com.finance.dart.api.abroad.dto.fmp.stockscreener.StockScreenerResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class StockScreenerService {

    private final FmpClientComponent fmpClientComponent;


    /**
     * 주식 스크리너
     * @param reqDto
     * @return
     */
    public List<StockScreenerResDto> findStockScreener(StockScreenerReqDto reqDto) {

         List<StockScreenerResDto> response = fmpClientComponent.sendGet(
                 FmpApiList.StockScreener,
                 reqDto,
                 new ParameterizedTypeReference<>() {}
         );

         if(response != null) {
             if(log.isDebugEnabled()) log.debug("조회 건수 = {}", response.size());
         }

         return response;
    }

}
