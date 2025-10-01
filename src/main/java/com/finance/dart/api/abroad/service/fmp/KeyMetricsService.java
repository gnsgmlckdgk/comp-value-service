package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.incomestatement.IncomeStatReqDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatement.IncomeStatResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class KeyMetricsService {

    private final FmpClientComponent fmpClientComponent;


    /**
     * 영업이익 조회
     * @param reqDto
     * @return
     */
    public List<IncomeStatResDto> findIncomeStat(IncomeStatReqDto reqDto) {

         List<IncomeStatResDto> response = fmpClientComponent.sendGet(
                 FmpApiList.IncomeStatement,
                 reqDto,
                 new ParameterizedTypeReference<>() {}
         );

         return response;
    }

}
