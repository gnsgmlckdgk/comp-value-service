package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.consts.FmpPeriod;
import com.finance.dart.api.abroad.dto.fmp.incomestatgrowth.IncomeStatGrowthReqDto;
import com.finance.dart.api.abroad.dto.fmp.incomestatgrowth.IncomeStatGrowthResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Service
public class IncomeStatGrowthService {

    private final FmpClientComponent fmpClientComponent;


    /**
     * 영업이익 성장률 조회
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

    /**
     * 영업이익 성장률 병렬 조회 (다건)
     * @param symbols 심볼 리스트
     * @return 심볼별 성장률 리스트
     */
    public Map<String, List<IncomeStatGrowthResDto>> findIncomeStatGrowthParallel(List<String> symbols) {
        List<IncomeStatGrowthReqDto> reqList = symbols.stream()
                .map(symbol -> {
                    IncomeStatGrowthReqDto req = new IncomeStatGrowthReqDto();
                    req.setSymbol(symbol);
                    req.setLimit(3);
                    req.setPeriod(FmpPeriod.annual);
                    return req;
                })
                .toList();

        return fmpClientComponent.sendGetParallel(
                FmpApiList.IncomeStatementGrowth,
                reqList,
                IncomeStatGrowthReqDto::getSymbol,
                new ParameterizedTypeReference<>() {});
    }

}
