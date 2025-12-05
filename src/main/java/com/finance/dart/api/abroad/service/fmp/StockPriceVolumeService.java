package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.chart.StockPriceVolumeReqDto;
import com.finance.dart.api.abroad.dto.fmp.chart.StockPriceVolumeResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 주식 가격 및 거래량 데이터 조회 서비스
 */
@AllArgsConstructor
@Service
public class StockPriceVolumeService {

    private final FmpClientComponent fmpClientComponent;

    /**
     * 주식 가격 및 거래량 데이터 조회
     * @param reqDto 요청 DTO (symbol 필수)
     * @return 주식 가격 및 거래량 데이터 목록
     */
    public List<StockPriceVolumeResDto> findStockPriceVolume(StockPriceVolumeReqDto reqDto) {

        List<StockPriceVolumeResDto> response = fmpClientComponent.sendGet(
                FmpApiList.StockPriceandVolumeData,
                reqDto,
                new ParameterizedTypeReference<>() {}
        );

        return response;
    }

}
