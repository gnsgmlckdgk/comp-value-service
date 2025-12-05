package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.chart.HistoricalIndexReqDto;
import com.finance.dart.api.abroad.dto.fmp.chart.HistoricalIndexResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 거래소 인덱스 차트 데이터 조회 서비스
 */
@AllArgsConstructor
@Service
public class HistoricalIndexService {

    private final FmpClientComponent fmpClientComponent;

    /**
     * 거래소 인덱스 차트 데이터 조회
     * @param reqDto 요청 DTO (symbol 필수)
     * @return 거래소 인덱스 차트 데이터 목록
     */
    public List<HistoricalIndexResDto> findHistoricalIndex(HistoricalIndexReqDto reqDto) {

        List<HistoricalIndexResDto> response = fmpClientComponent.sendGet(
                FmpApiList.HistoricalIndexFullChart,
                reqDto,
                new ParameterizedTypeReference<>() {}
        );

        return response;
    }

}
