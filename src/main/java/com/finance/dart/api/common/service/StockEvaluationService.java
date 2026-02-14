package com.finance.dart.api.common.service;

import com.finance.dart.api.abroad.dto.fmp.company.CompanyProfileDataResDto;
import com.finance.dart.api.abroad.service.US_StockCalFromFpmService;
import com.finance.dart.api.abroad.service.fmp.CompanyProfileSearchService;
import com.finance.dart.api.common.constants.EvaluationConst;
import com.finance.dart.api.common.dto.CompanySharePriceResult;
import com.finance.dart.api.common.dto.CompanySharePriceResultDetail;
import com.finance.dart.api.common.dto.evaluation.StepEvaluationDetail;
import com.finance.dart.api.common.dto.evaluation.StockEvaluationRequest;
import com.finance.dart.api.common.dto.evaluation.StockEvaluationResponse;
import com.finance.dart.common.component.RedisComponent;
import com.finance.dart.common.component.RedisKeyGenerator;
import com.finance.dart.common.util.StringUtil;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 종목 평가 서비스
 * Step 1~5 상세 평가를 통한 점수 산출
 */
@Slf4j
@AllArgsConstructor
@Service
public class StockEvaluationService {

    private final int MAX_EVALUATION_SYMBOLS = 50;  // 한번에 조회할 수 있는 최대 개수

    private final RedisComponent redisComponent;
    private final US_StockCalFromFpmService stockCalFromFpmService;
    private final CompanyProfileSearchService profileSearchService;

    /**
     * 종목 평가 (다건)
     * @param request 평가 요청 (심볼 리스트)
     * @return 평가 결과 리스트
     */
    public List<StockEvaluationResponse> evaluateStocks(StockEvaluationRequest request) {
        List<StockEvaluationResponse> responses = new ArrayList<>();

        if (request.getSymbols() == null || request.getSymbols().isEmpty()) {
            return responses;
        }

        if(request.getSymbols().size() > MAX_EVALUATION_SYMBOLS) {
            StockEvaluationResponse errorResponse = StockEvaluationResponse.builder()
                    .symbol("")
                    .totalScore(0.0)
                    .grade("ERROR")
                    .recommendation("평가 실패: 거래 당 " + MAX_EVALUATION_SYMBOLS + "건 이하로 조회가능합니다.")
                    .build();
            responses.add(errorResponse);
            return responses;
        }

        for (String symbol : request.getSymbols()) {
            try {
                StockEvaluationResponse response = evaluateSingleStock(symbol);
                responses.add(response);
            } catch (Exception e) {
                log.error("Failed to evaluate stock: {}", symbol, e);
                // 실패한 경우에도 빈 응답 추가 (에러 정보 포함)
                StockEvaluationResponse errorResponse = StockEvaluationResponse.builder()
                        .symbol(symbol)
                        .totalScore(0.0)
                        .grade("ERROR")
                        .recommendation("평가 실패: " + e.getMessage())
                        .build();
                responses.add(errorResponse);
            }
        }

        return responses;
    }

    /**
     * 단일 종목 평가
     * @param symbol 심볼
     * @return 평가 결과
     */
    private StockEvaluationResponse evaluateSingleStock(String symbol) throws Exception {

        // 1. Redis에서 데이터 조회 시도
        String redisKey = RedisKeyGenerator.genAbroadCompValueRstData(symbol, EvaluationConst.CAL_VALUE_VERSION);
        String cachedData = redisComponent.getValue(redisKey);

        CompanySharePriceResult result;
        if (!StringUtil.isStringEmpty(cachedData)) {
            // Redis에 데이터 있음
            result = new Gson().fromJson(cachedData, CompanySharePriceResult.class);
            log.debug("Loaded from Redis: {}", symbol);
        } else {
            // Redis에 데이터 없음 -> calPerValue 최신버전 실행
            result = stockCalFromFpmService.calPerValue(symbol);
            log.debug("Calculated new value: {}", symbol);
        }

        // 2. 기업 정보 조회
        CompanyProfileDataResDto profile = getCompanyProfile(symbol);

        // 3. Step 1~5 평가 수행
        CompanySharePriceResultDetail detail = result.get상세정보();
        String currentPrice = result.get현재가격();
        String fairValue = result.get주당가치();
        String calFairValue = result.get계산된주당가치();

        List<StepEvaluationDetail> stepDetails = new ArrayList<>();
        double step1Score = evaluateStep1(detail, stepDetails);
        double step2Score = evaluateStep2(detail, stepDetails);
        double step3Score = evaluateStep3(detail, stepDetails, currentPrice, fairValue);
        double step4Score = evaluateStep4(detail, stepDetails);
        double step5Score = evaluateStep5(detail, result, currentPrice, stepDetails);

        // 4. 총점 계산
        double totalScore = step1Score + step2Score + step3Score + step4Score + step5Score;

        // 5. 가격 차이 계산
        String priceDifference = calculatePriceDifference(currentPrice, fairValue);
        String priceGapPercent = calculatePriceGapPercent(currentPrice, fairValue);

        // 6. 등급 및 추천도 산정
        String grade = calculateGrade(totalScore);
        String recommendation = generateRecommendation(totalScore, detail);

        // 7. 응답 DTO 생성
        return StockEvaluationResponse.builder()
                .symbol(symbol)
                .companyName(result.get기업명())
                .currentPrice(currentPrice)
                .fairValue(fairValue)
                .calFairValue(calFairValue)
                .priceDifference(priceDifference)
                .priceGapPercent(priceGapPercent)
                .totalScore(totalScore)
                .grade(grade)
                .recommendation(recommendation)
                .peg(detail.getPEG())
                .per(detail.getPER())
                .sector(profile != null ? profile.getSector() : "N/A")
                .industry(profile != null ? profile.getIndustry() : "N/A")
                .beta(profile != null && profile.getBeta() != null ? profile.getBeta().toString() : "N/A")
                .exchange(profile != null ? profile.getExchange() : "N/A")
                .country(profile != null ? profile.getCountry() : "N/A")
                .marketCap(profile != null && profile.getMarketCap() != null ?
                        formatMarketCap(profile.getMarketCap()) : "N/A")
                .step1Score(step1Score)
                .step2Score(step2Score)
                .step3Score(step3Score)
                .step4Score(step4Score)
                .step5Score(step5Score)
                .stepDetails(stepDetails)
                .resultDetail(detail)
                .calVersion(result.get버전())
                .build();
    }

    /**
     * Step 1: 위험 신호 확인 (15점) - 치명적 결함 필터
     * - 수익가치계산불가: 0점
     * - 적자기업 + 매출기반평가: 6점
     * - 적자기업: 7.5점
     * - 매출기반평가: 9점
     * - 정상 기업: 15점
     */
    private double evaluateStep1(CompanySharePriceResultDetail detail, List<StepEvaluationDetail> stepDetails) {
        double score = EvaluationConst.STEP1_WEIGHT;
        StringBuilder details = new StringBuilder();

        if (detail.is수익가치계산불가()) {
            score = 0;
            details.append("❌ 수익가치 계산 불가 (적정가 신뢰도 매우 낮음, 0점). ");
        } else if (detail.is적자기업() && detail.is매출기반평가()) {
            score = 6;
            details.append("⚠️ 적자기업이며 매출 기반 평가 (리스크 높음, ")
                    .append(String.format("%.1f", score)).append("점). ");
        } else if (detail.is적자기업()) {
            score = 7.5;
            details.append("⚠️ 적자기업 (투자 위험, ")
                    .append(String.format("%.1f", score)).append("점). ");
        } else if (detail.is매출기반평가()) {
            score = 9;
            details.append("⚠️ 매출 기반 평가 (이익 없이 매출만 큰 기업, 과대평가 가능성, ")
                    .append(String.format("%.1f", score)).append("점). ");
        } else {
            details.append("✅ 정상 기업 (PER 기반 정상 계산, 만점 ")
                    .append(EvaluationConst.STEP1_WEIGHT).append("점). ");
        }

        if (detail.is흑자전환기업()) {
            details.append("📈 흑자 전환 기업 (긍정적 신호). ");
        }

        stepDetails.add(StepEvaluationDetail.builder()
                .stepNumber(1)
                .stepName("위험 신호 확인")
                .score(score)
                .maxScore(EvaluationConst.STEP1_WEIGHT)
                .description(EvaluationConst.STEP1_DESC)
                .details(details.toString())
                .build());

        return score;
    }

    /**
     * Step 2: 신뢰도 확인 (20점)
     * - PER 정상 범위 (5~30): +7점
     * - 순부채 건전 (음수 또는 낮음): +8점
     * - 영업이익 안정성: +5점
     */
    private double evaluateStep2(CompanySharePriceResultDetail detail, List<StepEvaluationDetail> stepDetails) {
        double score = 0;
        StringBuilder details = new StringBuilder();

        // PER 평가 (7점)
        String perStr = detail.getPER();
        if (!StringUtil.isStringEmpty(perStr) && !"N/A".equals(perStr)) {
            try {
                double per = Double.parseDouble(perStr);
                if (per >= EvaluationConst.PER_MIN_NORMAL && per <= EvaluationConst.PER_MAX_NORMAL) {
                    score += 7;
                    details.append(String.format("✅ PER %.2f (정상 범위 5~30, +7점). ", per));
                } else if (per < EvaluationConst.PER_HIGH_RISK) {
                    score += 4;
                    details.append(String.format("⚠️ PER %.2f (보통, +4점). ", per));
                } else {
                    score += 1;
                    details.append(String.format("❌ PER %.2f (고평가 가능성, +1점). ", per));
                }
            } catch (Exception e) {
                details.append("PER 정보 없음 (+0점). ");
            }
        } else {
            details.append("PER 정보 없음 (+0점). ");
        }

        // 순부채 평가 (8점)
        String netDebtStr = detail.get순부채();
        if (!StringUtil.isStringEmpty(netDebtStr) && !"N/A".equals(netDebtStr)) {
            try {
                double netDebt = Double.parseDouble(netDebtStr);
                if (netDebt < 0) {
                    score += 8;
                    details.append("✅ 순부채 음수 (현금이 부채보다 많음, 매우 건전, +8점). ");
                } else if (netDebt < 100000000000.0) {  // 1000억 미만
                    score += 5;
                    details.append("✅ 순부채 건전 (+5점). ");
                } else {
                    score += 2;
                    details.append("⚠️ 순부채 높음 (+2점). ");
                }
            } catch (Exception e) {
                details.append("순부채 정보 없음 (+0점). ");
            }
        } else {
            details.append("순부채 정보 없음 (+0점). ");
        }

        // 영업이익 안정성 (5점)
        String op1 = detail.get영업이익_전전기();
        String op2 = detail.get영업이익_전기();
        String op3 = detail.get영업이익_당기();

        if (!StringUtil.isStringEmpty(op1) && !StringUtil.isStringEmpty(op2) && !StringUtil.isStringEmpty(op3)
                && !"N/A".equals(op1) && !"N/A".equals(op2) && !"N/A".equals(op3)) {
            try {
                double o1 = Double.parseDouble(op1);
                double o2 = Double.parseDouble(op2);
                double o3 = Double.parseDouble(op3);

                if (o1 > 0 && o2 > 0 && o3 > 0 && o2 >= o1 && o3 >= o2) {
                    score += 5;
                    details.append("✅ 영업이익 3년 연속 흑자 및 증가 추세 (+5점). ");
                } else if (o1 > 0 && o2 > 0 && o3 > 0) {
                    score += 3;
                    details.append("✅ 영업이익 3년 연속 흑자 (+3점). ");
                } else {
                    score += 1;
                    details.append("⚠️ 영업이익 불안정 (+1점). ");
                }
            } catch (Exception e) {
                details.append("영업이익 정보 오류 (+0점). ");
            }
        } else {
            details.append("영업이익 정보 없음 (+0점). ");
        }

        stepDetails.add(StepEvaluationDetail.builder()
                .stepNumber(2)
                .stepName("신뢰도 확인")
                .score(score)
                .maxScore(EvaluationConst.STEP2_WEIGHT)
                .description(EvaluationConst.STEP2_DESC)
                .details(details.toString())
                .build());

        return score;
    }

    /**
     * Step 3: 밸류에이션 평가 (30점)
     * - PEG 평가: 최대 12점
     * - 가격 차이(저평가 여부): 최대 10점
     * - 성장률 지속가능성: 최대 8점
     */
    private double evaluateStep3(CompanySharePriceResultDetail detail, List<StepEvaluationDetail> stepDetails,
                                  String currentPrice, String fairValue) {
        double score = 0;
        StringBuilder details = new StringBuilder();

        // 1. PEG 평가 (12점)
        String pegStr = detail.getPEG();
        if (!StringUtil.isStringEmpty(pegStr) && !"N/A".equals(pegStr) && !"999".equals(pegStr)) {
            try {
                double peg = Double.parseDouble(pegStr);
                if (peg < 0.5) {
                    score += 12;
                    details.append(String.format("🌟 PEG %.2f (매우 저평가, +12점). ", peg));
                } else if (peg < 0.8) {
                    score += 10;
                    details.append(String.format("✅ PEG %.2f (저평가, +10점). ", peg));
                } else if (peg < 1.0) {
                    score += 8;
                    details.append(String.format("✅ PEG %.2f (양호, +8점). ", peg));
                } else if (peg < 1.2) {
                    score += 5;
                    details.append(String.format("⚠️ PEG %.2f (적정, +5점). ", peg));
                } else if (peg < 1.5) {
                    score += 3;
                    details.append(String.format("⚠️ PEG %.2f (보통, +3점). ", peg));
                } else if (peg < 2.0) {
                    score += 1;
                    details.append(String.format("⚠️ PEG %.2f (고평가 위험, +1점). ", peg));
                } else {
                    score += 0;
                    details.append(String.format("❌ PEG %.2f (과대평가, +0점). ", peg));
                }
            } catch (Exception e) {
                details.append("PEG 정보 오류 (+0점). ");
            }
        } else {
            details.append("PEG 정보 없음 (+0점). ");
        }

        // 2. 가격 차이 평가 (10점) - 저평가 여부
        if (!StringUtil.isStringEmpty(currentPrice) && !StringUtil.isStringEmpty(fairValue)) {
            try {
                BigDecimal current = new BigDecimal(currentPrice);
                BigDecimal fair = new BigDecimal(fairValue);

                if (current.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal diff = fair.subtract(current);
                    BigDecimal gapPercent = diff.divide(current, 4, RoundingMode.HALF_UP)
                            .multiply(new BigDecimal("100"));
                    double gap = gapPercent.doubleValue();

                    if (gap >= 30) {
                        score += 10;
                        details.append(String.format("🌟 가격차이 %.1f%% (크게 저평가, +10점). ", gap));
                    } else if (gap >= 20) {
                        score += 8;
                        details.append(String.format("✅ 가격차이 %.1f%% (저평가, +8점). ", gap));
                    } else if (gap >= 10) {
                        score += 6;
                        details.append(String.format("✅ 가격차이 %.1f%% (약간 저평가, +6점). ", gap));
                    } else if (gap >= 0) {
                        score += 3;
                        details.append(String.format("⚠️ 가격차이 %.1f%% (적정가 수준, +3점). ", gap));
                    } else if (gap >= -10) {
                        score += 1;
                        details.append(String.format("⚠️ 가격차이 %.1f%% (약간 고평가, +1점). ", gap));
                    } else {
                        score += 0;
                        details.append(String.format("❌ 가격차이 %.1f%% (고평가, +0점). ", gap));
                    }
                }
            } catch (Exception e) {
                details.append("가격 차이 계산 오류 (+0점). ");
            }
        } else {
            details.append("가격 정보 없음 (+0점). ");
        }

        // 3. 성장률 지속가능성 평가 (8점)
        String growthStr = detail.get영업이익성장률();
        if (!StringUtil.isStringEmpty(growthStr) && !"N/A".equals(growthStr)) {
            try {
                double growth = Double.parseDouble(growthStr);
                double growthPct = growth * 100;
                if (growth < 0) {
                    score += 1;
                    details.append(String.format("❌ 영업이익 성장률 %.1f%% (역성장, +1점). ", growthPct));
                } else if (growthPct <= 5) {
                    score += 3;
                    details.append(String.format("⚠️ 영업이익 성장률 %.1f%% (저성장, +3점). ", growthPct));
                } else if (growthPct <= 15) {
                    score += 6;
                    details.append(String.format("✅ 영업이익 성장률 %.1f%% (안정 성장, +6점). ", growthPct));
                } else if (growthPct <= 50) {
                    score += 8;
                    details.append(String.format("🌟 영업이익 성장률 %.1f%% (고성장, +8점). ", growthPct));
                } else if (growthPct <= 80) {
                    score += 4;
                    details.append(String.format("⚠️ 영업이익 성장률 %.1f%% (과도 성장, 지속 어려울 수 있음, +4점). ", growthPct));
                } else {
                    score += 1;
                    details.append(String.format("❌ 영업이익 성장률 %.1f%% (매우 높음, 일시적 급증 가능성, +1점). ", growthPct));
                }
            } catch (Exception e) {
                details.append("성장률 정보 오류 (+0점). ");
            }
        } else {
            details.append("성장률 정보 없음 (+0점). ");
        }

        stepDetails.add(StepEvaluationDetail.builder()
                .stepNumber(3)
                .stepName("밸류에이션 평가")
                .score(score)
                .maxScore(EvaluationConst.STEP3_WEIGHT)
                .description(EvaluationConst.STEP3_DESC)
                .details(details.toString())
                .build());

        return score;
    }

    /**
     * Step 4: 영업이익 추세 확인 (15점)
     * - 3년간 영업이익 추세 분석
     */
    private double evaluateStep4(CompanySharePriceResultDetail detail, List<StepEvaluationDetail> stepDetails) {
        double score = 0;
        StringBuilder details = new StringBuilder();

        String op1 = detail.get영업이익_전전기();
        String op2 = detail.get영업이익_전기();
        String op3 = detail.get영업이익_당기();

        if (!StringUtil.isStringEmpty(op1) && !StringUtil.isStringEmpty(op2) && !StringUtil.isStringEmpty(op3)
                && !"N/A".equals(op1) && !"N/A".equals(op2) && !"N/A".equals(op3)) {
            try {
                double o1 = Double.parseDouble(op1);
                double o2 = Double.parseDouble(op2);
                double o3 = Double.parseDouble(op3);

                // 당기 실적이 급증했는지 확인 (일회성 가능성)
                if (o3 > o2 * 2.5 && o2 <= o1 * 1.2) {
                    score += 3;
                    details.append(String.format("⚠️ 당기 실적 급증 (전전기: %.0f억, 전기: %.0f억, 당기: %.0f억). " +
                            "일회성 실적일 가능성, 적정가 과대 산출 위험 (+3점). ",
                            o1 / 100000000, o2 / 100000000, o3 / 100000000));
                } else if (o1 > 0 && o2 >= o1 && o3 >= o2) {
                    // 꾸준한 증가 추세 - 성장률 크기에 따라 차등
                    double avgGrowth = ((o3 / o1) - 1) / 2;  // 2년 평균 성장률
                    double avgGrowthPct = avgGrowth * 100;
                    if (avgGrowthPct >= 15) {
                        score += 15;
                    } else if (avgGrowthPct >= 5) {
                        score += 12;
                    } else {
                        score += 10;
                    }
                    details.append(String.format("✅ 영업이익 꾸준한 증가 추세 (전전기: %.0f억, 전기: %.0f억, 당기: %.0f억, " +
                            "2년 평균 성장률: %.1f%%, +%.0f점). ",
                            o1 / 100000000, o2 / 100000000, o3 / 100000000, avgGrowthPct, score));
                } else if (o1 > 0 && o2 > 0 && o3 > 0) {
                    // 흑자이지만 등락
                    score += 9;
                    details.append(String.format("✅ 영업이익 흑자 유지하나 등락 있음 (전전기: %.0f억, 전기: %.0f억, 당기: %.0f억, +9점). ",
                            o1 / 100000000, o2 / 100000000, o3 / 100000000));
                } else if (o3 > 0) {
                    // 최근 흑자 전환
                    score += 6;
                    details.append(String.format("⚠️ 최근 흑자 전환 (전전기: %.0f억, 전기: %.0f억, 당기: %.0f억, +6점). ",
                            o1 / 100000000, o2 / 100000000, o3 / 100000000));
                } else {
                    score += 1;
                    details.append("❌ 영업이익 적자 지속 (+1점). ");
                }
            } catch (Exception e) {
                details.append("영업이익 추세 분석 오류 (+0점). ");
            }
        } else {
            details.append("영업이익 데이터 부족 (+0점). ");
        }

        stepDetails.add(StepEvaluationDetail.builder()
                .stepNumber(4)
                .stepName("영업이익 추세 확인")
                .score(score)
                .maxScore(EvaluationConst.STEP4_WEIGHT)
                .description(EvaluationConst.STEP4_DESC)
                .details(details.toString())
                .build());

        return score;
    }

    /**
     * Step 5: 투자 적합성 (20점) - NEW
     * - 매수적정가 vs 현재가: 최대 8점
     * - PEG/PSR 이진 판단: 최대 7점
     * - 그레이엄 기준: 최대 5점
     */
    private double evaluateStep5(CompanySharePriceResultDetail detail, CompanySharePriceResult result,
                                  String currentPrice, List<StepEvaluationDetail> stepDetails) {
        double score = 0;
        StringBuilder details = new StringBuilder();

        // 과대평가 단계적 감점: 적정가 대비 고평가 비율에 따라 PEG/PSR·그레이엄 점수 감산
        // 0%~-10%: 감점 없음, -10%~-20%: 50% 감점, -20%~-30%: 75% 감점, -30% 이하: 100% 차단
        double overvaluedPenalty = 0.0;  // 0.0=감점없음, 1.0=전액차단
        String fairValueStr = result.get주당가치();
        if (!StringUtil.isStringEmpty(fairValueStr) && !StringUtil.isStringEmpty(currentPrice)) {
            try {
                double fair = Double.parseDouble(fairValueStr);
                double current = Double.parseDouble(currentPrice);
                if (fair > 0 && current > 0) {
                    double gapPct = (fair - current) / current * 100;
                    if (gapPct < -30) {
                        overvaluedPenalty = 1.0;
                    } else if (gapPct < -20) {
                        overvaluedPenalty = 0.75;
                    } else if (gapPct < -10) {
                        overvaluedPenalty = 0.5;
                    }
                }
            } catch (Exception ignored) {}
        }

        // 1. 매수적정가 vs 현재가 (8점)
        String purchasePriceStr = result.get매수적정가();
        if (!StringUtil.isStringEmpty(purchasePriceStr) && !StringUtil.isStringEmpty(currentPrice)) {
            try {
                double purchasePrice = Double.parseDouble(purchasePriceStr);
                double current = Double.parseDouble(currentPrice);
                double fair = !StringUtil.isStringEmpty(fairValueStr) ? Double.parseDouble(fairValueStr) : 0;

                if (purchasePrice > current) {
                    score += EvaluationConst.STEP5_PURCHASE_PRICE;  // 8점
                    details.append(String.format("🌟 매수적정가 $%.2f > 현재가 $%.2f (매수 적합, +%d점). ",
                            purchasePrice, current, EvaluationConst.STEP5_PURCHASE_PRICE));
                } else if (fair > current) {
                    score += 3;
                    details.append(String.format("✅ 적정가 $%.2f > 현재가 $%.2f (상승여력 있음, +3점). ",
                            fair, current));
                } else {
                    details.append(String.format("❌ 매수적정가 $%.2f ≤ 현재가 $%.2f (+0점). ",
                            purchasePrice, current));
                }
            } catch (Exception e) {
                details.append("매수적정가 정보 오류 (+0점). ");
            }
        } else {
            details.append("매수적정가 정보 없음 (+0점). ");
        }

        // 2. PEG/PSR 이진 판단 (7점) - 고평가 시 단계적 감점
        {
            int rawPegPsrScore = 0;
            boolean is매출기반 = detail.is매출기반평가();
            if (is매출기반) {
                String psrStr = detail.getPSR();
                if (!StringUtil.isStringEmpty(psrStr) && !"N/A".equals(psrStr)) {
                    try {
                        double psr = Double.parseDouble(psrStr);
                        if (psr > 0 && psr < 2) {
                            rawPegPsrScore = EvaluationConst.STEP5_PEG_PSR_BINARY;  // 7점
                        }
                        if (rawPegPsrScore == 0) {
                            details.append(String.format("❌ PSR %.2f ≥ 2 (+0점). ", psr));
                        }
                    } catch (Exception e) {
                        details.append("PSR 정보 오류 (+0점). ");
                    }
                } else {
                    details.append("PSR 정보 없음 (+0점). ");
                }
            } else {
                String pegStr = detail.getPEG();
                if (!StringUtil.isStringEmpty(pegStr) && !"N/A".equals(pegStr) && !"999".equals(pegStr)) {
                    try {
                        double peg = Double.parseDouble(pegStr);
                        if (peg > 0 && peg <= 1.0) {
                            rawPegPsrScore = EvaluationConst.STEP5_PEG_PSR_BINARY;  // 7점
                        }
                        if (rawPegPsrScore == 0) {
                            details.append(String.format("❌ PEG %.2f > 1 (+0점). ", peg));
                        }
                    } catch (Exception e) {
                        details.append("PEG 정보 오류 (+0점). ");
                    }
                } else {
                    details.append("PEG 정보 없음 (+0점). ");
                }
            }
            if (rawPegPsrScore > 0) {
                int adjusted = (int) Math.round(rawPegPsrScore * (1.0 - overvaluedPenalty));
                score += adjusted;
                String label = is매출기반 ? "PSR" : "PEG";
                if (overvaluedPenalty > 0) {
                    details.append(String.format("⚠️ %s 기준 충족하나 고평가 감점 %d%% 적용 (+%d점). ",
                            label, (int)(overvaluedPenalty * 100), adjusted));
                } else {
                    details.append(String.format("✅ %s 기준 충족 (+%d점). ", label, adjusted));
                }
            }
        }

        // 3. 그레이엄 기준 (5점) - 고평가 시 단계적 감점
        int grahamPassCount = detail.get그레이엄_통과수();
        {
            int rawGrahamScore = 0;
            if (grahamPassCount >= 5) {
                rawGrahamScore = 5;
            } else if (grahamPassCount >= 4) {
                rawGrahamScore = 3;
            } else if (grahamPassCount >= 3) {
                rawGrahamScore = 1;
            }
            if (rawGrahamScore > 0) {
                int adjusted = (int) Math.round(rawGrahamScore * (1.0 - overvaluedPenalty));
                score += adjusted;
                if (overvaluedPenalty > 0) {
                    details.append(String.format("⚠️ 그레이엄 %d/5 통과하나 고평가 감점 %d%% 적용 (+%d점). ",
                            grahamPassCount, (int)(overvaluedPenalty * 100), adjusted));
                } else {
                    String icon = grahamPassCount >= 5 ? "🌟" : (grahamPassCount >= 4 ? "✅" : "⚠️");
                    details.append(icon + " 그레이엄 " + grahamPassCount + "/5 통과 (+" + adjusted + "점). ");
                }
            } else {
                details.append("❌ 그레이엄 " + grahamPassCount + "/5 통과 (+0점). ");
            }
        }

        stepDetails.add(StepEvaluationDetail.builder()
                .stepNumber(5)
                .stepName("투자 적합성")
                .score(score)
                .maxScore(EvaluationConst.STEP5_WEIGHT)
                .description(EvaluationConst.STEP5_DESC)
                .details(details.toString())
                .build());

        return score;
    }

    /**
     * 등급 계산
     * @param totalScore 총점
     * @return 등급 (S, A, B, C, D, F)
     */
    private String calculateGrade(double totalScore) {
        if (totalScore >= 90) return "S";
        if (totalScore >= 80) return "A";
        if (totalScore >= 70) return "B";
        if (totalScore >= 60) return "C";
        if (totalScore >= 50) return "D";
        return "F";
    }

    /**
     * 투자 추천도 생성
     * @param totalScore 총점
     * @param detail 상세정보
     * @return 추천도 메시지
     */
    private String generateRecommendation(double totalScore, CompanySharePriceResultDetail detail) {
        if (detail.is수익가치계산불가()) {
            return "⛔ 투자 비추천: 수익가치 계산 불가, 리스크 매우 높음";
        }

        if (totalScore >= 85) {
            return "🌟 강력 매수 추천: 저평가 + 재무 건전성 우수 + 성장성 우수";
        } else if (totalScore >= 75) {
            return "✅ 매수 추천: 안정적 재무구조 + 합리적 밸류에이션";
        } else if (totalScore >= 65) {
            return "👍 매수 고려 가능: 전반적으로 양호하나 일부 주의 필요";
        } else if (totalScore >= 55) {
            return "⚠️ 신중한 검토 필요: 리스크 요인 존재, 추가 분석 권장";
        } else if (totalScore >= 45) {
            return "⚠️ 투자 주의: 여러 리스크 요인 존재";
        } else {
            return "🚫 투자 비추천: 높은 리스크, 투자 적합하지 않음";
        }
    }

    /**
     * 기업 프로필 조회
     * @param symbol 심볼
     * @return 기업 프로필
     */
    private CompanyProfileDataResDto getCompanyProfile(String symbol) {
        try {
            List<CompanyProfileDataResDto> profiles = profileSearchService.findProfileListBySymbol(symbol);
            if (profiles != null && !profiles.isEmpty()) {
                return profiles.get(0);
            }
        } catch (Exception e) {
            log.warn("Failed to get company profile for {}", symbol, e);
        }
        return null;
    }

    /**
     * 가격 차이 계산 (절대값)
     * @param currentPrice 현재가
     * @param fairValue 적정가
     * @return 가격 차이
     */
    private String calculatePriceDifference(String currentPrice, String fairValue) {
        if (StringUtil.isStringEmpty(currentPrice) || StringUtil.isStringEmpty(fairValue)) {
            return "N/A";
        }

        try {
            BigDecimal current = new BigDecimal(currentPrice);
            BigDecimal fair = new BigDecimal(fairValue);
            BigDecimal diff = fair.subtract(current);
            return diff.setScale(2, RoundingMode.HALF_UP).toPlainString();
        } catch (Exception e) {
            log.warn("Failed to calculate price difference", e);
            return "N/A";
        }
    }

    /**
     * 가격 차이 비율 계산 (%)
     * @param currentPrice 현재가
     * @param fairValue 적정가
     * @return 가격 차이 비율
     */
    private String calculatePriceGapPercent(String currentPrice, String fairValue) {
        if (StringUtil.isStringEmpty(currentPrice) || StringUtil.isStringEmpty(fairValue)) {
            return "N/A";
        }

        try {
            BigDecimal current = new BigDecimal(currentPrice);
            BigDecimal fair = new BigDecimal(fairValue);

            if (current.compareTo(BigDecimal.ZERO) <= 0) {
                return "N/A";
            }

            BigDecimal diff = fair.subtract(current);
            BigDecimal percent = diff.divide(current, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            return percent.setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
        } catch (Exception e) {
            log.warn("Failed to calculate price gap percent", e);
            return "N/A";
        }
    }

    /**
     * 시가총액 포맷팅
     * @param marketCap 시가총액
     * @return 포맷된 문자열
     */
    private String formatMarketCap(Long marketCap) {
        if (marketCap == null) return "N/A";

        if (marketCap >= 1_000_000_000_000L) {
            return String.format("$%.2fT", marketCap / 1_000_000_000_000.0);
        } else if (marketCap >= 1_000_000_000L) {
            return String.format("$%.2fB", marketCap / 1_000_000_000.0);
        } else if (marketCap >= 1_000_000L) {
            return String.format("$%.2fM", marketCap / 1_000_000.0);
        } else {
            return String.format("$%d", marketCap);
        }
    }

}
