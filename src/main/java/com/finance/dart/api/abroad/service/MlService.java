package com.finance.dart.api.abroad.service;

import com.finance.dart.api.abroad.consts.PredictionProgramConfig;
import com.finance.dart.api.abroad.dto.ml.ExternalPredictionResDto;
import com.finance.dart.api.abroad.dto.ml.LogContentDto;
import com.finance.dart.api.abroad.dto.ml.LogFileInfoDto;
import com.finance.dart.api.abroad.dto.ml.LogFileListDto;
import com.finance.dart.api.abroad.dto.ml.PredictionResponseDto;
import com.finance.dart.api.common.constants.EvaluationConst;
import com.finance.dart.api.common.entity.StockPredictionEntity;
import com.finance.dart.api.common.repository.StockPredictionRepository;
import com.finance.dart.common.component.RedisComponent;
import com.finance.dart.common.component.RedisKeyGenerator;
import com.finance.dart.common.util.DateUtil;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 머신러닝 예측 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class MlService {

    private final StockPredictionRepository stockPredictionRepository;
    private final RedisComponent redisComponent;

    @Value("${app.local}")
    private boolean isLocal;

    /**
     * ML 예측 API 전용 RestTemplate (ReadTimeout 60초)
     */
    private RestTemplate createMlRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);    // 5초
        factory.setReadTimeout(100000);     // 100초 (학습 후 응답하는 경우 시간이 걸림)
        return new RestTemplate(factory);
    }

    /**
     * <pre>
     * 1주일 내 최고가 AI 예측 조회
     * 화면 출력용 - DB 데이터가 더 필요해서 DB 한번 더 조회
     * 머신러닝에서 학습 후 DB에 데이터 등록함
     * </pre>
     * @param symbol
     * @param isLearning
     * @return
     */
    public PredictionResponseDto getPredictionForWeb(String symbol, boolean isLearning) {

        PredictionResponseDto predictionResponseDto = getPrediction(symbol, isLearning);

        if(predictionResponseDto != null) {
            LocalDate today = LocalDate.now();
            Optional<StockPredictionEntity> existingPrediction =
                    stockPredictionRepository.findByTickerAndPredictionDate(symbol, today);

            if (existingPrediction.isPresent()) {
                if (log.isDebugEnabled()) {
                    log.debug("DB에서 예측 데이터 조회 완료 - symbol: {}", symbol);
                }

                // 다음 조회때 캐싱되지 않게 하기위해(예측데이터 없는 데이터로 등록되어있어서) 캐싱데이터 삭제
                redisComponent.deleteKey(RedisKeyGenerator.genAbroadCompValueRstData(symbol, EvaluationConst.CAL_VALUE_VERSION));

                return convertEntityToDto(existingPrediction.get(), predictionResponseDto.getSource());
            }
        }

        return null;
    }

    /**
     * 1주일 내 최고가 AI 예측 조회
     *
     * @param symbol 티커 심볼
     * @param isLearning 학습 여부(테이블 데이터 없을시)
     * @return 예측 결과
     */
    public PredictionResponseDto getPrediction(String symbol, boolean isLearning) {

        if (log.isDebugEnabled()) {
            log.debug("AI 예측 조회 시작 - symbol: {}, isLocal: {}", symbol, isLocal);
        }

        // 1. DB에서 오늘 날짜 데이터 조회
        LocalDate today = LocalDate.now();
        Optional<StockPredictionEntity> existingPrediction =
                stockPredictionRepository.findByTickerAndPredictionDate(symbol, today);

        // 2. DB에 데이터가 있으면 해당 데이터 반환
        if (existingPrediction.isPresent()) {
            if (log.isDebugEnabled()) {
                log.debug("DB에서 예측 데이터 조회 완료 - symbol: {}", symbol);
            }
            return convertEntityToDto(existingPrediction.get(), "database");
        }

        // 3. DB에 데이터가 없으면 외부 파드 호출
        if (log.isDebugEnabled()) {
            log.debug("DB에 데이터 없음. 외부 API 호출 여부 [{}] - symbol: {}", isLearning, symbol);
        }

        if(!isLearning) return null;

        ExternalPredictionResDto externalResponse = callExternalPredictionApi(symbol);

        // 4. 에러 응답 처리
        if (externalResponse.getError() != null) {
            throw new RuntimeException("Prediction error: " + externalResponse.getError());
        }

        // 5. 외부 API 응답을 DTO로 변환하여 반환
        return convertExternalResponseToDto(externalResponse);
    }

    /**
     * 외부 예측 API 호출 (ReadTimeout 60초)
     *
     * @param symbol 티커 심볼
     * @return 외부 API 응답
     */
    private ExternalPredictionResDto callExternalPredictionApi(String symbol) {

        // 환경에 따라 URL 결정
        String baseUrl = isLocal
                ? PredictionProgramConfig.localHost
                : PredictionProgramConfig.prodHost;

        String predictAPI = PredictionProgramConfig.API_URI_predict;

        String url = String.format("%s/%s/%s", baseUrl, predictAPI, symbol);

        if (log.isDebugEnabled()) {
            log.debug("외부 예측 API 호출 - URL: {}, ReadTimeout: 60초", url);
        }

        try {
            // ML 예측 전용 RestTemplate 사용 (ReadTimeout 60초)
            RestTemplate mlRestTemplate = createMlRestTemplate();

            URI uri = URI.create(url);
            ResponseEntity<ExternalPredictionResDto> response = mlRestTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ExternalPredictionResDto>() {
                    }
            );

            ExternalPredictionResDto body = response.getBody();

            if (log.isDebugEnabled()) {
                log.debug("외부 예측 API 응답 - response: {}", body);
            }

            return body;

        } catch (Exception e) {
            log.error("외부 예측 API 호출 실패 - symbol: {}, error: {}", symbol, e.getMessage(), e);
            throw new RuntimeException("Failed to call external prediction API", e);
        }
    }

    /**
     * 엔티티를 응답 DTO로 변환
     *
     * @param entity 엔티티
     * @param source 데이터 소스
     * @return 응답 DTO
     */
    private PredictionResponseDto convertEntityToDto(StockPredictionEntity entity, String source) {

        // upside_percent 계산
        String upsidePercent = calculateUpsidePercent(entity.getCurrentPrice(), entity.getPredictedHigh());

        return PredictionResponseDto.builder()
                .ticker(entity.getTicker())
                .companyName(entity.getCompanyName())
                .exchange(entity.getExchange())
                .predictedHigh(entity.getPredictedHigh())
                .currentPrice(entity.getCurrentPrice())
                .upsidePercent(upsidePercent)
                .predictionDate(DateUtil.convertLocaleDateToString(entity.getPredictionDate(), "yyyy-MM-dd"))
                .targetStartDate(DateUtil.convertLocaleDateToString(entity.getTargetStartDate(), "yyyy-MM-dd"))
                .targetEndDate(DateUtil.convertLocaleDateToString(entity.getTargetEndDate(), "yyyy-MM-dd"))
                .source(source)
                .modelVersion(entity.getModelVersion())
                .build();
    }

    /**
     * 외부 API 응답을 응답 DTO로 변환
     *
     * @param externalResponse 외부 API 응답
     * @return 응답 DTO
     */
    private PredictionResponseDto convertExternalResponseToDto(ExternalPredictionResDto externalResponse) {

        BigDecimal currentPrice = StringUtils.hasText(externalResponse.getCurrentPrice())
                ? new BigDecimal(externalResponse.getCurrentPrice())
                : null;

        BigDecimal predictedHigh = StringUtils.hasText(externalResponse.getPredictedHigh1w())
                ? new BigDecimal(externalResponse.getPredictedHigh1w())
                : null;

        return PredictionResponseDto.builder()
                .ticker(externalResponse.getTicker())
                .predictedHigh(predictedHigh)
                .currentPrice(currentPrice)
                .upsidePercent(externalResponse.getUpsidePercent())
                .source(externalResponse.getSource())
                .build();
    }

    /**
     * 상승 여력(%) 계산
     *
     * @param currentPrice  현재가
     * @param predictedHigh 예측 최고가
     * @return 상승 여력 (%)
     */
    private String calculateUpsidePercent(BigDecimal currentPrice, BigDecimal predictedHigh) {

        if (currentPrice == null || predictedHigh == null ||
                currentPrice.compareTo(BigDecimal.ZERO) == 0) {
            return "0.00%";
        }

        BigDecimal upside = predictedHigh.subtract(currentPrice)
                .divide(currentPrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(new BigDecimal("100"));

        return String.format("%.2f%%", upside);
    }

    /**
     * 로그 파일 목록 조회
     *
     * @return 로그 파일 목록
     */
    public LogFileListDto getLogFileList() {

        String baseUrl = isLocal
                ? PredictionProgramConfig.localHost
                : PredictionProgramConfig.prodHost;

        String url = String.format("%s/%s", baseUrl, PredictionProgramConfig.API_URI_logs);

        if (log.isDebugEnabled()) {
            log.debug("로그 파일 목록 조회 API 호출 - URL: {}", url);
        }

        try {
            RestTemplate restTemplate = createMlRestTemplate();
            URI uri = URI.create(url);
            ResponseEntity<LogFileListDto> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<LogFileListDto>() {}
            );

            LogFileListDto body = response.getBody();

            if (log.isDebugEnabled()) {
                log.debug("로그 파일 목록 조회 완료 - response: {}", body);
            }

            return body;

        } catch (Exception e) {
            log.error("로그 파일 목록 조회 실패 - error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get log file list", e);
        }
    }

    /**
     * 최신 로그 파일 정보 조회
     *
     * @return 최신 로그 파일 정보
     */
    public LogFileInfoDto getLatestLogFileInfo() {

        String baseUrl = isLocal
                ? PredictionProgramConfig.localHost
                : PredictionProgramConfig.prodHost;

        String url = String.format("%s/%s", baseUrl, PredictionProgramConfig.API_URI_logs_latest);

        if (log.isDebugEnabled()) {
            log.debug("최신 로그 파일 정보 조회 API 호출 - URL: {}", url);
        }

        try {
            RestTemplate restTemplate = createMlRestTemplate();
            URI uri = URI.create(url);
            ResponseEntity<LogFileInfoDto> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<LogFileInfoDto>() {}
            );

            LogFileInfoDto body = response.getBody();

            if (log.isDebugEnabled()) {
                log.debug("최신 로그 파일 정보 조회 완료 - response: {}", body);
            }

            return body;

        } catch (Exception e) {
            log.error("최신 로그 파일 정보 조회 실패 - error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get latest log file info", e);
        }
    }

    /**
     * 로그 파일 내용 조회
     *
     * @param filename 로그 파일명
     * @return 로그 내용
     */
    public LogContentDto getLogContent(String filename) {

        String baseUrl = isLocal
                ? PredictionProgramConfig.localHost
                : PredictionProgramConfig.prodHost;

        String url = String.format("%s/%s/%s", baseUrl, PredictionProgramConfig.API_URI_logs, filename);

        //2 파라미터 정의
        url = url + "?all=true";    // all : 로그 전체 조회

        if (log.isDebugEnabled()) {
            log.debug("로그 내용 조회 API 호출 - URL: {}, filename: {}", url, filename);
        }

        try {
            RestTemplate restTemplate = createMlRestTemplate();
            URI uri = URI.create(url);
            ResponseEntity<LogContentDto> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<LogContentDto>() {}
            );

            LogContentDto body = response.getBody();

            if (log.isDebugEnabled()) {
                log.debug("로그 내용 조회 완료 - filename: {}", filename);
            }

            return body;

        } catch (Exception e) {
            log.error("로그 내용 조회 실패 - filename: {}, error: {}", filename, e.getMessage(), e);
            throw new RuntimeException("Failed to get log content", e);
        }
    }

    /**
     * 최신 로그 증분 조회 (폴링용)
     *
     * @param lastLine 마지막으로 읽은 줄 번호 (0이면 초기 로드)
     * @return 최신 로그 내용
     */
    public LogContentDto getLatestLogStream(Integer lastLine) {

        String baseUrl = isLocal
                ? PredictionProgramConfig.localHost
                : PredictionProgramConfig.prodHost;

        String url = String.format("%s/%s?lastLine=%d",
                baseUrl,
                PredictionProgramConfig.API_URI_logs_stream_latest,
                lastLine != null ? lastLine : 0);

        if (log.isDebugEnabled()) {
            log.debug("최신 로그 조회 API 호출 - URL: {}, lastLine: {}", url, lastLine);
        }

        try {
            RestTemplate restTemplate = createMlRestTemplate();
            URI uri = URI.create(url);
            ResponseEntity<LogContentDto> response = restTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<LogContentDto>() {}
            );

            LogContentDto body = response.getBody();

            if (log.isDebugEnabled()) {
                log.debug("최신 로그 조회 완료 - endLine: {}, totalLines: {}",
                        body != null ? body.getEndLine() : null,
                        body != null ? body.getTotalLines() : null);
            }

            return body;

        } catch (Exception e) {
            log.error("최신 로그 조회 실패 - lastLine: {}, error: {}", lastLine, e.getMessage(), e);
            throw new RuntimeException("Failed to get latest log stream", e);
        }
    }
}
