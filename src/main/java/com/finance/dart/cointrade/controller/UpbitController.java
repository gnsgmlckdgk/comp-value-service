package com.finance.dart.cointrade.controller;

import com.finance.dart.cointrade.dto.upbit.TradingParisDto;
import com.finance.dart.cointrade.service.UpbitService;
import com.finance.dart.common.config.EndPointConfig;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.member.enums.RoleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
    @GetMapping("/market/all")
    public ResponseEntity<CommonResponse<List<TradingParisDto>>> getAllMarket() {

        List<TradingParisDto> tradingParisDtoList = upbitService.getTradingPairs();

        return new ResponseEntity<>(new CommonResponse<>(tradingParisDtoList), HttpStatus.OK);
    }

}
