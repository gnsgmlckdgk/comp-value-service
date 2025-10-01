package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.balancesheet.BalanceSheetReqDto;
import com.finance.dart.api.abroad.dto.fmp.balancesheet.BalanceSheetResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class BalanceSheetStatementService {

    private final FmpClientComponent fmpClientComponent;


    /**
     * 재무상태표 조회
     * @param reqDto
     * @return
     */
    public List<BalanceSheetResDto> findBalanceSheet(BalanceSheetReqDto reqDto) {

         List<BalanceSheetResDto> response = fmpClientComponent.sendGet(
                 FmpApiList.BalanceSheetStatement,
                 reqDto,
                 new ParameterizedTypeReference<>() {}
         );

         return response;
    }

}
