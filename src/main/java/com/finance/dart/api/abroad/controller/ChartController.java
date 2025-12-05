package com.finance.dart.api.abroad.controller;

import com.finance.dart.api.abroad.dto.fmp.chart.HistoricalIndexReqDto;
import com.finance.dart.api.abroad.dto.fmp.chart.HistoricalIndexResDto;
import com.finance.dart.api.abroad.dto.fmp.chart.StockPriceVolumeReqDto;
import com.finance.dart.api.abroad.dto.fmp.chart.StockPriceVolumeResDto;
import com.finance.dart.api.abroad.service.fmp.HistoricalIndexService;
import com.finance.dart.api.abroad.service.fmp.StockPriceVolumeService;
import com.finance.dart.common.dto.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 차트 데이터 조회 컨트롤러
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("abroad/chart")
public class ChartController {

    private final StockPriceVolumeService stockPriceVolumeService;
    private final HistoricalIndexService historicalIndexService;

    /**
     * 주식 가격 및 거래량 데이터 조회
     * @param reqDto 요청 DTO
     * @return 주식 가격 및 거래량 데이터 목록
     */
    @PostMapping("/stock-price-volume")
    public ResponseEntity<CommonResponse<List<StockPriceVolumeResDto>>> getStockPriceVolume(
            @RequestBody StockPriceVolumeReqDto reqDto) {

        List<StockPriceVolumeResDto> response = stockPriceVolumeService.findStockPriceVolume(reqDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 거래소 인덱스 차트 데이터 조회
     * @param reqDto 요청 DTO
     * @return 거래소 인덱스 차트 데이터 목록
     */
    @PostMapping("/historical-index")
    public ResponseEntity<CommonResponse<List<HistoricalIndexResDto>>> getHistoricalIndex(
            @RequestBody HistoricalIndexReqDto reqDto) {

        List<HistoricalIndexResDto> response = historicalIndexService.findHistoricalIndex(reqDto);

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

}
