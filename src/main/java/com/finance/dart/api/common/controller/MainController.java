package com.finance.dart.api.common.controller;

import com.finance.dart.api.abroad.service.US_StockCalFromFpmService;
import com.finance.dart.api.abroad.service.US_StockCalFromSecService;
import com.finance.dart.api.common.dto.CompanySharePriceCalculator;
import com.finance.dart.api.common.dto.CompanySharePriceResult;
import com.finance.dart.api.common.dto.RecommendedStocksReqDto;
import com.finance.dart.api.common.dto.evaluation.StockEvaluationRequest;
import com.finance.dart.api.common.dto.evaluation.StockEvaluationResponse;
import com.finance.dart.api.common.service.PerShareValueCalculationService;
import com.finance.dart.api.common.service.RecommendedCompanyService;
import com.finance.dart.api.common.service.StockEvaluationService;
import com.finance.dart.api.common.service.schedule.RecommendedStocksProcessor;
import com.finance.dart.api.domestic.service.DomesticStockCalculationService;
import com.finance.dart.common.constant.ResponseEnum;
import com.finance.dart.common.dto.CommonResponse;
import com.finance.dart.common.exception.BizException;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@RequestMapping("main")
@RestController
public class MainController {

    private final DomesticStockCalculationService domesticStockCalculationService;  // 국내주식계산 서비스
    private final US_StockCalFromSecService US_StockCalFromSecService;              // 미국주식계산 서비스(SEC)
    private final US_StockCalFromFpmService US_StockCalFromFmpService;              // 미국주식계산 서비스(FMP)
    private final PerShareValueCalculationService perShareValueCalculationService;  // 가치계산 서비스
    private final RecommendedCompanyService recommendedCompanyService;              // 기업추천 서비스
    private final StockEvaluationService stockEvaluationService;                    // 종목평가 서비스


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
            response = domesticStockCalculationService.calPerValue(year, corpCode, corpName);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if(log.isDebugEnabled()) log.debug("response = {}", response);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * 국내 기업의 한주당 가치 수동 계산
     * @param companySharePriceCalculator
     * @return
     */
    @PostMapping("/cal/per_value/manual")
    public ResponseEntity<Map> calCompanyStockPerValueManual(
            @RequestBody CompanySharePriceCalculator companySharePriceCalculator
    ) {

        String response = perShareValueCalculationService.calPerValue(companySharePriceCalculator);
        if(log.isDebugEnabled()) log.debug("response = {}", response);

        Map<String, Object> responseMap = new LinkedHashMap<>();
        responseMap.put("result", response);

        return new ResponseEntity<>(responseMap, HttpStatus.OK);
    }

    /**
     * <pre>
     * 해외기업
     * 한 기업의 한주당 가치 계산 (최신)
     * </pre>
     * @param symbol
     * @return
     * @throws Exception
     */
    @GetMapping("/cal/per_value/abroad")
    public ResponseEntity<Object> calAbroadCompanyStockPerValue(@RequestParam("symbol") String symbol)
            throws Exception {

        // 버전 변경시 EvaluationConst.java 상수 수정 필요
        CompanySharePriceResult responseBody = US_StockCalFromFmpService.calPerValue(symbol);   // FMP

        return new ResponseEntity<>(new CommonResponse<>(responseBody), HttpStatus.OK);
    }

    /**
     * <pre>
     * 해외기업
     * 한 기업의 한주당 가치 계산 다건 (최신)
     * </pre>
     * @param symbolList
     * @param detail
     * @return
     * @throws Exception
     */
    @GetMapping("/cal/per_value/abroad/arr")
    public ResponseEntity<Object> calAbroadCompanyStockPerValueArr(@RequestParam("symbol") String symbolList,
                                                                     @Nullable @RequestParam("detail") String detail)
            throws Exception {

        // 버전 변경시 EvaluationConst.java 상수 수정 필요
        List<CompanySharePriceResult> responseBody = US_StockCalFromFmpService.calPerValueList(symbolList, detail);   // FMP

        return new ResponseEntity<>(new CommonResponse<>(responseBody), HttpStatus.OK);
    }


    /**
     * 추천 미국 거래소 기업 조회 (프로파일별)
     * @param recommendedStocksReqDto 요청 DTO (profileName 필수)
     * @return
     */
    @PostMapping("/rem/usstock")
    public ResponseEntity<CommonResponse<List<RecommendedStocksProcessor.RecommendedStockData>>>
    findRecommenedCompany(@RequestBody RecommendedStocksReqDto recommendedStocksReqDto) {

        // profileName 필수 체크
        if (recommendedStocksReqDto.getProfileName() == null ||
            recommendedStocksReqDto.getProfileName().trim().isEmpty()) {
            throw new BizException(ResponseEnum.EMPTY_REQ_PARAM);
        }

        List<RecommendedStocksProcessor.RecommendedStockData> data =
                recommendedCompanyService.getAbroadCompanyByProfile(recommendedStocksReqDto.getProfileName());

        CommonResponse<List<RecommendedStocksProcessor.RecommendedStockData>> response =
                new CommonResponse<>(data);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * <pre>
     * 해외기업 종목 평가 (다건)
     * Step 1~4 상세 평가를 통한 100점 만점 점수 산출
     * - Step 1 (20점): 위험 신호 확인 (치명적 결함 필터)
     * - Step 2 (25점): 신뢰도 확인 (재무 건전성)
     * - Step 3 (40점): 밸류에이션 평가 (PEG, 가격차이, 성장률) ⭐ 가장 중요
     * - Step 4 (15점): 영업이익 추세 확인 (성장 지속가능성)
     *
     * 등급: S(90+), A(80+), B(70+), C(60+), D(50+), F(50미만)
     * </pre>
     * @param request 평가 요청 (심볼 리스트)
     * @return 평가 결과 리스트
     */
    @PostMapping("/evaluate/stocks")
    public ResponseEntity<CommonResponse<List<StockEvaluationResponse>>> evaluateStocks(
            @RequestBody StockEvaluationRequest request) {

        List<StockEvaluationResponse> responseBody = stockEvaluationService.evaluateStocks(request);

        return new ResponseEntity<>(new CommonResponse<>(responseBody), HttpStatus.OK);
    }

}
