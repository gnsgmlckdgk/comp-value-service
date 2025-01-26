package com.finance.dart.controller;

import com.finance.dart.dto.StockValueResultDTO;
import com.finance.dart.service.CalCompanyStockPerValueService;
import com.finance.dart.service.CalPerValueService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@AllArgsConstructor
@RequestMapping("main")
@RestController
public class MainController {

    private final CalPerValueService calPerValueService;
    private final CalCompanyStockPerValueService calCompanyStockPerValueService;

    /**
     * 헬스체크
     * @return
     */
    @GetMapping("/check")
    public ResponseEntity<Object> healthCheck() {
        return new ResponseEntity<>("Check OK", HttpStatus.OK);
    }

    /**
     * 전체 한주당 가치 계산
     * @param year
     * @return
     */
    @GetMapping("/cal/all/per_value")
    public ResponseEntity<Object> calPerValue(
            @RequestParam("year") String year
    ) {

        // TODO: 개발중단 [2025.01.16 한번에 여러번 호출 시 요청 차된되서 이 방법은 힘들거 같음]
        Object response = calPerValueService.calPerValue(year);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 한 기업의 한주당 가치 계산
     * @param year
     * @param corpCode
     * @param corpName
     * @return
     */
    @GetMapping("/cal/per_value")
    public ResponseEntity<StockValueResultDTO> calCompanyStockPerValue(
            @RequestParam("year") String year,
            @RequestParam(value = "corp_code", defaultValue = "") String corpCode,
            @RequestParam(value = "corp_name", defaultValue = "") String corpName
    ) {

        log.debug("year={}, corp_code={}, corp_name={}", year, corpCode, corpName);

        StockValueResultDTO response = calCompanyStockPerValueService.calPerValue(year, corpCode, corpName);
        log.debug("response = {}", response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
