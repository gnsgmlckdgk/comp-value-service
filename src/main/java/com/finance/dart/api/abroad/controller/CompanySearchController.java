package com.finance.dart.api.abroad.controller;

import com.finance.dart.api.abroad.dto.fmp.company.CompanyProfileDataResDto;
import com.finance.dart.api.abroad.dto.fmp.company.FindCompanySymbolResDto;
import com.finance.dart.api.abroad.service.fmp.CompanyProfileSearchService;
import com.finance.dart.api.abroad.service.fmp.CompanySymbolSearchService;
import com.finance.dart.common.dto.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 해외기업 검색 컨트롤러
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("abroad/company/search")
public class CompanySearchController {

    private final CompanySymbolSearchService companySymbolSearchService;
    private final CompanyProfileSearchService companyProfileSearchService;

    /**
     * 해외기업 심볼 검색
     * @param companyName 기업명
     * @return
     */
    @GetMapping("/symbol")
    public ResponseEntity<CommonResponse<List<FindCompanySymbolResDto>>> findSymbolByCompanyName(@RequestParam(name = "cn") String companyName) {

        List<FindCompanySymbolResDto> companySymbolList = companySymbolSearchService.findSymbolListByCompanyName(companyName);

        return new ResponseEntity<>(new CommonResponse<>(companySymbolList), HttpStatus.OK);
    }

    /**
     * 해외기업 프로파일 검색
     * @param symbol 기업심볼
     * @return
     */
    @GetMapping("/profile")
    public ResponseEntity<CommonResponse<List<CompanyProfileDataResDto>>> findProfileBySymbol(@RequestParam(name = "symbol") String symbol) {

        List<CompanyProfileDataResDto> profileList = companyProfileSearchService.findProfileListBySymbol(symbol);

        return new ResponseEntity<>(new CommonResponse<>(profileList), HttpStatus.OK);
    }

}
