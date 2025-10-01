package com.finance.dart.api.abroad.service;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosReqDto;
import com.finance.dart.api.abroad.dto.fmp.financialratios.FinancialRatiosResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class FinancialRatiosService {

    private final FmpClientComponent fmpClientComponent;


    /**
     * 재무비율지표 조회
     * @param reqDto
     * @return
     */
    public List<FinancialRatiosResDto> findFinancialRatios(FinancialRatiosReqDto reqDto) {

         List<FinancialRatiosResDto> response = fmpClientComponent.sendGet(
                 FmpApiList.FinancialRatios,
                 reqDto,
                 new ParameterizedTypeReference<>() {}
         );

         return response;
    }

}
