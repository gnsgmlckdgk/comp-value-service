package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.incomestatgrowth.IncomeStatGrowthReqDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatgrowth.IncomeStatGrowthResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class IncomeStatGrowthService {

    private final FmpClientComponent fmpClientComponent;


    /**
     * 재무재표 성장률 조회
     * @param reqDto
     * @return
     */
    public List<IncomeStatGrowthResDto> findIncomeStatGrowth(IncomeStatGrowthReqDto reqDto) {

        List<IncomeStatGrowthResDto> response = fmpClientComponent.sendGet(
                FmpApiList.IncomeStatementGrowth,
                reqDto,
                new ParameterizedTypeReference<>() {}
        );

        return response;
    }

}
