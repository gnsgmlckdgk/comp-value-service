package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.analystestimates.AnalystEstimatesReqDto;
import com.finance.dart.api.abroad.dto.fmp.analystestimates.AnalystEstimatesResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class AnalystEstimatesService {

    private final FmpClientComponent fmpClientComponent;

    /**
     * 애널리스트 추정치 조회
     * @param reqDto 요청 DTO
     * @return 애널리스트 추정치 리스트
     */
    public List<AnalystEstimatesResDto> findAnalystEstimates(AnalystEstimatesReqDto reqDto) {

        List<AnalystEstimatesResDto> response = fmpClientComponent.sendGet(
                FmpApiList.AnalystEstimates,
                reqDto,
                new ParameterizedTypeReference<>() {}
        );

        return response;
    }

}
