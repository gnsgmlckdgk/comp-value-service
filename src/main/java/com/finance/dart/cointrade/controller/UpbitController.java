package com.finance.dart.cointrade.controller;

import com.finance.dart.cointrade.dto.upbit.*;
import com.finance.dart.cointrade.service.UpbitService;
import com.finance.dart.common.config.EndPointConfig;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.common.logging.TransactionLogging;
import com.finance.dart.member.enums.RoleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/upbit")
@RequiredArgsConstructor
public class UpbitController {

    private final UpbitService upbitService;


    /**
     * 마켓 정보 조회
     * @return
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/market/all")
    public ResponseEntity<CommonResponse<List<TradingParisDto>>> getAllMarket() {

        List<TradingParisDto> tradingParisDtoList = upbitService.getTradingPairs();

        return new ResponseEntity<>(new CommonResponse<>(tradingParisDtoList), HttpStatus.OK);
    }

    /**
     * 페어 단위 현재가 조회
     * @param markets KRW-BTC,KRW-ETH,BTC-ETH,BTC-XRP
     * @return
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/v1/ticker")
    public ResponseEntity<CommonResponse<List<TickerDto>>> getTicker(@RequestParam(name = "markets") String markets) {

        List<TickerDto> tickerDtoList = upbitService.getTicker(markets);

        return new ResponseEntity<>(new CommonResponse<>(tickerDtoList), HttpStatus.OK);
    }

    /**
     * 일(Day) 캔들 조회
     * @param reqDto
     * @return
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/candles/days")
    public ResponseEntity<CommonResponse<List<CandleDayResDto>>> getCandlesDays(CandleReqDto reqDto) {
        List<CandleDayResDto> response = upbitService.getCandlesDays(reqDto);
        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 주(Week) 캔들 조회
     * @param reqDto
     * @return
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/candles/weeks")
    public ResponseEntity<CommonResponse<List<CandleWeekResDto>>> getCandlesWeeks(CandleReqDto reqDto) {
        List<CandleWeekResDto> response = upbitService.getCandlesWeeks(reqDto);
        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 월(Month) 캔들 조회
     * @param reqDto
     * @return
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/candles/months")
    public ResponseEntity<CommonResponse<List<CandleMonthResDto>>> getCandlesMonths(CandleReqDto reqDto) {
        List<CandleMonthResDto> response = upbitService.getCandlesMonths(reqDto);
        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 연(Year) 캔들 조회
     * @param reqDto
     * @return
     */
    @EndPointConfig.RequireRole({RoleConstants.ROLE_SUPER_ADMIN})
    @TransactionLogging
    @GetMapping("/candles/years")
    public ResponseEntity<CommonResponse<List<CandleYearResDto>>> getCandlesYears(CandleReqDto reqDto) {
        List<CandleYearResDto> response = upbitService.getCandlesYears(reqDto);
        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

}
