package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.financialgrowth.FinancialGrowthReqDto;
import com.finance.dart.api.abroad.dto.fmp.financialgrowth.FinancialGrowthResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class FinancialGrowthService {

    private final FmpClientComponent fmpClientComponent;


    /**
     * 성장률 조회
     * @param reqDto
     * @return
     */
    public List<FinancialGrowthResDto> financialStatementsGrowth(FinancialGrowthReqDto reqDto) {

        List<FinancialGrowthResDto> response = fmpClientComponent.sendGet(
                FmpApiList.FinancialStatementsGrowth,
                reqDto,
                new ParameterizedTypeReference<>() {}
        );

        return response;
    }

}
