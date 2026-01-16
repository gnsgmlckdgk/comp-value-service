package com.finance.dart.api.abroad.controller;


import com.finance.dart.api.abroad.dto.ml.PredictionResponseDto;
import com.finance.dart.api.abroad.service.MlService;
import com.finance.dart.common.dto.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


/**
 * 머신러닝 거래 컨트롤러
 */
@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("ml")
public class MlController {

    private final MlService mlService;

    /**
     * AI 예측 - 1주일 내 최고가
     * @param symbol 티커 심볼
     * @return 예측 결과
     */
    @GetMapping("/predict")
    public ResponseEntity<CommonResponse<PredictionResponseDto>> predict(@RequestParam(name = "symbol") String symbol) {

        if(log.isDebugEnabled()) log.debug("/ml/predict 거래 요청 symbol = {}", symbol);

        try {
            PredictionResponseDto prediction = mlService.getPrediction(symbol, true);

            CommonResponse<PredictionResponseDto> response = new CommonResponse<>(prediction);
            response.setMessage("AI 예측 조회 성공");

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("AI 예측 조회 실패 - symbol: {}, error: {}", symbol, e.getMessage(), e);

            CommonResponse<PredictionResponseDto> errorResponse = new CommonResponse<>();
            errorResponse.setMessage("AI 예측 조회 실패: " + e.getMessage());

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * <pre>
     * AI 예측 - 1주일 내 최고가
     * 화면 출력용
     * </pre>
     * @param symbol
     * @return
     */
    @GetMapping("/predict/w")
    public ResponseEntity<CommonResponse<PredictionResponseDto>> predictForWeb(@RequestParam(name = "symbol") String symbol) {

        if(log.isDebugEnabled()) log.debug("/ml/predict 거래 요청 symbol = {}", symbol);

        try {
            PredictionResponseDto prediction = mlService.getPredictionForWeb(symbol, true);

            CommonResponse<PredictionResponseDto> response = new CommonResponse<>(prediction);
            response.setMessage("AI 예측 조회 성공");

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("AI 예측 조회 실패 - symbol: {}, error: {}", symbol, e.getMessage(), e);

            CommonResponse<PredictionResponseDto> errorResponse = new CommonResponse<>();
            errorResponse.setMessage("AI 예측 조회 실패: " + e.getMessage());

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
