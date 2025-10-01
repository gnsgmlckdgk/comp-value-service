package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.keymetrics.KeyMetricsReqDto;
import com.finance.dart.api.abroad.dto.fmp.keymetrics.KeyMetricsResDto;
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
     * 주요 재무지표 조회
     * @param reqDto
     * @return
     */
    public List<KeyMetricsResDto> findKeyMetrics(KeyMetricsReqDto reqDto) {

         List<KeyMetricsResDto> response = fmpClientComponent.sendGet(
                 FmpApiList.KeyMetrics,
                 reqDto,
                 new ParameterizedTypeReference<>() {}
         );

         return response;
    }

}
