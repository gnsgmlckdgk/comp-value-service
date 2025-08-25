package com.finance.dart.api.common.controller;

import com.finance.dart.api.abroad.service.OverseasStockValueService;
import com.finance.dart.api.common.dto.CompanySharePriceCalculator;
import com.finance.dart.api.common.dto.CompanySharePriceResult;
import com.finance.dart.api.common.service.CompanySharePriceCalculatorService;
import com.finance.dart.api.domestic.service.CalCompanyStockPerValueService;
import com.finance.dart.common.dto.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RequestMapping("main")
@RestController
public class MainController {

    // TODO: 클래스명, 패키지명 리팩토링 예정 (main -> 각 패키지로 이동, 클래스명 국내, 해외 명확하게)
    private final CalCompanyStockPerValueService calCompanyStockPerValueService;            // 국내주식계산 서비스
    private final OverseasStockValueService overseasStockValueService;                      // 해외주식계산 서비스
    private final CompanySharePriceCalculatorService companySharePriceCalculatorService;    // 가치계산 서비스


    /**
     * 헬스체크
     * @return
     */
    @GetMapping("/check")
    public ResponseEntity<Object> healthCheck() {
        log.info("HealthCheck!!!_헬스체크!!!");
        return new ResponseEntity<>("Check OK", HttpStatus.OK);
    }

    /**
     * <pre>
     * 국내기업
     * 한 기업의 한주당 가치 계산
     * </pre>
     * @param year
     * @param corpCode
     * @param corpName
     * @return
     */
    @GetMapping("/cal/per_value")
    public ResponseEntity<CompanySharePriceResult> calDomesticCompanyStockPerValue(
            @RequestParam("year") String year,
            @RequestParam(value = "corp_code", defaultValue = "") String corpCode,
            @RequestParam(value = "corp_name", defaultValue = "") String corpName
    ) {

        if(log.isDebugEnabled()) log.debug("year={}, corp_code={}, corp_name={}", year, corpCode, corpName);

        CompanySharePriceResult response = null;
        try {
            response = calCompanyStockPerValueService.calPerValue(year, corpCode, corpName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(log.isDebugEnabled()) log.debug("response = {}", response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * <pre>
     * 해외기업
     * 한 기업의 한주당 가치 계산
     * </pre>
     * @param symbol
     * @return
     */
    @GetMapping("/cal/per_value/abroad")
    public ResponseEntity<Object> calAbroadCompanyStockPerValue(@RequestParam("symbol") String symbol) {

        CompanySharePriceResult responseBody =  overseasStockValueService.calPerValue(symbol);

        return new ResponseEntity<>(new CommonResponse<>(responseBody), HttpStatus.OK);
    }


    /**
     * 한 기업의 한주당 가치 수동 계산
     * @param companySharePriceCalculator
     * @return
     */
    @PostMapping("/cal/per_value/manual")
    public ResponseEntity<Map> calCompanyStockPerValueManual(
            @RequestBody CompanySharePriceCalculator companySharePriceCalculator
            ) {

        String response = companySharePriceCalculatorService.calPerValue(companySharePriceCalculator);
        if(log.isDebugEnabled()) log.debug("response = {}", response);

        Map<String, Object> responseMap = new LinkedHashMap<>();
        responseMap.put("result", response);

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    }

}
