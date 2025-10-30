package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.quote.AfterTradeReqDto;
import com.finance.dart.api.abroad.dto.fmp.quote.AfterTradeResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class AfterTradeService {

    private final FmpClientComponent fmpClientComponent;


    /**
     * 애프터마켓 시세 조회
     * @param reqDto
     * @return
     */
    public List<AfterTradeResDto> findAfterTrade(AfterTradeReqDto reqDto) {

         List<AfterTradeResDto> response = fmpClientComponent.sendGet(
                 FmpApiList.AftermarketTrade,
                 reqDto,
                 new ParameterizedTypeReference<>() {}
         );

         return response;
    }

}
