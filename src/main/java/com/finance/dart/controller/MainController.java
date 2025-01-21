package com.finance.dart.controller;

import com.finance.dart.dto.CorpCodeDTO;
import com.finance.dart.dto.StockValueResultDTO;
import com.finance.dart.service.CalCompanyStockPerValueService;
import com.finance.dart.service.CalPerValueService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
     * @param corpCode
     * @param year
     * @return
     */
    @GetMapping("/cal/per_value")
    public ResponseEntity<StockValueResultDTO> calCompanyStockPerValue(
            @RequestParam("year") String year,
            @RequestParam("corp_code") String corpCode
    ) {

        StockValueResultDTO response = calCompanyStockPerValueService.calPerValue(year, corpCode);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
