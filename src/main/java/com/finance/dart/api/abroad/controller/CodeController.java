package com.finance.dart.api.abroad.controller;


import com.finance.dart.api.abroad.dto.fmp.codelist.CountriesDto;
import com.finance.dart.api.abroad.dto.fmp.codelist.ExchangesDto;
import com.finance.dart.api.abroad.dto.fmp.codelist.IndustiresDto;
import com.finance.dart.api.abroad.dto.fmp.codelist.SectorsDto;
import com.finance.dart.api.abroad.service.fmp.CodeListService;
import com.finance.dart.common.dto.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("abroad/code")
public class CodeController {

    private final CodeListService codeListService;


    /**
     * 거래소 목록조회
     * @return
     */
    @GetMapping("/exchanges")
    public ResponseEntity<CommonResponse<List<ExchangesDto>>> exchanges() {

        List<ExchangesDto> response = codeListService.findExchanges();

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 섹터 목록조회
     * @return
     */
    @GetMapping("/sectors")
    public ResponseEntity<CommonResponse<List<SectorsDto>>> sectors() {

        List<SectorsDto> response = codeListService.findSectors();

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }

    /**
     * 산업 목록조회
     * @return
     */
    @GetMapping("/industries")
    public ResponseEntity<CommonResponse<List<IndustiresDto>>> industries() {

        List<IndustiresDto> response = codeListService.findIndustries();

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }


    /**
     * 국가코드 목록조회
     * @return
     */
    @GetMapping("/countries")
    public ResponseEntity<CommonResponse<List<CountriesDto>>> countries() {

        List<CountriesDto> response = codeListService.findCountries();

        return new ResponseEntity<>(new CommonResponse<>(response), HttpStatus.OK);
    }


}
