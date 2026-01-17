package com.finance.dart.api.abroad.controller;


import com.finance.dart.api.abroad.dto.ml.LogContentDto;
import com.finance.dart.api.abroad.dto.ml.LogFileInfoDto;
import com.finance.dart.api.abroad.dto.ml.LogFileListDto;
import com.finance.dart.api.abroad.dto.ml.PredictionResponseDto;
import com.finance.dart.api.abroad.service.MlService;
import com.finance.dart.common.dto.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    /**
     * 로그 파일 목록 조회
     * @return 로그 파일 목록
     */
    @GetMapping("/logs")
    public ResponseEntity<CommonResponse<LogFileListDto>> getLogFileList() {

        if(log.isDebugEnabled()) log.debug("/ml/logs 거래 요청");

        try {
            LogFileListDto logFileList = mlService.getLogFileList();

            CommonResponse<LogFileListDto> response = new CommonResponse<>(logFileList);
            response.setMessage("로그 파일 목록 조회 성공");

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("로그 파일 목록 조회 실패 - error: {}", e.getMessage(), e);

            CommonResponse<LogFileListDto> errorResponse = new CommonResponse<>();
            errorResponse.setMessage("로그 파일 목록 조회 실패: " + e.getMessage());

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 최신 로그 파일 정보 조회
     * @return 최신 로그 파일 정보
     */
    @GetMapping("/logs/latest")
    public ResponseEntity<CommonResponse<LogFileInfoDto>> getLatestLogFileInfo() {

        if(log.isDebugEnabled()) log.debug("/ml/logs/latest 거래 요청");

        try {
            LogFileInfoDto logFileInfo = mlService.getLatestLogFileInfo();

            CommonResponse<LogFileInfoDto> response = new CommonResponse<>(logFileInfo);
            response.setMessage("최신 로그 파일 정보 조회 성공");

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("최신 로그 파일 정보 조회 실패 - error: {}", e.getMessage(), e);

            CommonResponse<LogFileInfoDto> errorResponse = new CommonResponse<>();
            errorResponse.setMessage("최신 로그 파일 정보 조회 실패: " + e.getMessage());

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 로그 파일 내용 조회
     * @param filename 로그 파일명
     * @return 로그 내용
     */
    @GetMapping("/logs/{filename}")
    public ResponseEntity<CommonResponse<LogContentDto>> getLogContent(@PathVariable(name = "filename") String filename) {

        if(log.isDebugEnabled()) log.debug("/ml/logs/{} 거래 요청 - filename: {}", filename, filename);

        try {
            LogContentDto logContent = mlService.getLogContent(filename);

            CommonResponse<LogContentDto> response = new CommonResponse<>(logContent);
            response.setMessage("로그 내용 조회 성공");

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("로그 내용 조회 실패 - filename: {}, error: {}", filename, e.getMessage(), e);

            CommonResponse<LogContentDto> errorResponse = new CommonResponse<>();
            errorResponse.setMessage("로그 내용 조회 실패: " + e.getMessage());

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 최신 로그 증분 조회 (폴링용)
     * @param lastLine 마지막으로 읽은 줄 번호 (0이면 초기 로드)
     * @return 최신 로그 내용
     */
    @GetMapping("/logs/stream/latest")
    public ResponseEntity<CommonResponse<LogContentDto>> getLatestLogStream(
            @RequestParam(name = "lastLine", required = false, defaultValue = "0") Integer lastLine) {

        if(log.isDebugEnabled()) log.debug("/ml/logs/stream/latest 거래 요청 - lastLine: {}", lastLine);

        try {
            LogContentDto logContent = mlService.getLatestLogStream(lastLine);

            CommonResponse<LogContentDto> response = new CommonResponse<>(logContent);
            response.setMessage("최신 로그 조회 성공");

            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (Exception e) {
            log.error("최신 로그 조회 실패 - lastLine: {}, error: {}", lastLine, e.getMessage(), e);

            CommonResponse<LogContentDto> errorResponse = new CommonResponse<>();
            errorResponse.setMessage("최신 로그 조회 실패: " + e.getMessage());

            return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
