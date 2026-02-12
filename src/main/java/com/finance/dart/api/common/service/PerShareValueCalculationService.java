package com.finance.dart.api.common.service;

import com.finance.dart.api.abroad.service.sec.SecFinStatementService;
import com.finance.dart.api.common.constants.RequestContextConst;
import com.finance.dart.api.common.context.RequestContext;
import com.finance.dart.api.common.dto.CompanySharePriceCalculator;
import com.finance.dart.api.common.dto.CompanySharePriceResultDetail;
import com.finance.dart.api.common.dto.SectorCalculationParameters;
import com.finance.dart.common.util.CalUtil;
import com.finance.dart.common.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 한 주당 가치 계산 서비스
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PerShareValueCalculationService {

    private final RequestContext requestContext;

    private final SecFinStatementService financialStatementService;     // 해외기업 재무제표 조회 서비스


    /**
     * 한 주당 가치를 계산한다. V2
     *
     * @param req
     * @return
     */
    public String calPerValueV2(CompanySharePriceCalculator req, CompanySharePriceResultDetail resultDetail) {

        if(log.isDebugEnabled()) log.debug("CompanySharePriceCalculator = {}", req);

        final String per = req.getPer();                                    // PER
        final String incomGrowth = req.getOperatingIncomeGrowth();          // 영업이익 성장율
        final String epsGrowth = req.getEpsgrowth();                        // EPS 성장율
        final String assetsTotal = req.getCurrentAssetsTotal();             // 유동자산합계
        final String liabilitiesTotal = req.getCurrentLiabilitiesTotal();   // 유동부채합계
        final String intangibleAssets = req.getIntangibleAssets();          // 무형자산
        final String totalDebt = req.getTotalDebt();                        // 총부채
        final String cash = req.getCashAndCashEquivalents();                // 현금성 자산
        final String currentRatio = req.getCurrentRatio();                  // 유동비율
        final String investmentAssets = req.getInvestmentAssets();          // 투자자산 (비유동자산 내)
        final String issuedShares = req.getIssuedShares();                  // 발행주식수

        final String K = StringUtil.defaultString(getLiabilityFactor(Double.parseDouble(currentRatio)));    // 유동부채 차감 비율

        // STEP01 ------------------------------------------------------------------------------------------------------
        // (영업이익 3년 평균 × 성장률 보정 PER)
        // 1. 영업이익 3년 평균 계산
        final String operatingProfitAvg = calOperatingProfitAvg(
                req.getOperatingProfitPrePre(),
                req.getOperatingProfitPre(),
                req.getOperatingProfitCurrent()
        );

        // 2. 성장률 보정 PER
        // PER에 성장률(g)을 단순 가산하여 적정 PER을 보정
        // - A식 (÷g)은 이론상 정밀하지만 변동이 과도함
        // - B식 (×(1+g))은 보수적이면서 실제 시장 밸류에 근접

        //String adjustedPER = calAdjustedPER(incomGrowth, per);    // A
        String adjustedPER = CalUtil.multi(per, CalUtil.add("1", incomGrowth)); // B

        final String STEP01 = CalUtil.multi(operatingProfitAvg, adjustedPER);

        resultDetail.setPER(per);
        resultDetail.set영업이익성장률(incomGrowth);
        resultDetail.set성장률보정PER(adjustedPER);
        resultDetail.setPEG(calPeg(per, epsGrowth));  // PEG 는 영업이익 성장률이 아닌 EPS 성장률

        // STEP02 ------------------------------------------------------------------------------------------------------
        // (유동자산 − (유동부채 × 비율) + 투자자산)
        String assets = CalUtil.sub(assetsTotal, cash);   // 2025.10.22: STEP05 에서 현금계산이 포함되어있어서 이중계산되서 빼줌
        final String STEP02 = CalUtil.add(CalUtil.sub(assets, CalUtil.multi(liabilitiesTotal, K)), investmentAssets);

        // STEP03 ------------------------------------------------------------------------------------------------------
        // (무형자산 × 30%)
        final String STEP03 = CalUtil.multi(intangibleAssets, "0.3");

        // STEP04 ------------------------------------------------------------------------------------------------------
        // (최근 3년 평균 R&D)
        final String STEP04 = calRnDAvg(req.getRndPrePre(), req.getRndPre(), req.getRndCurrent());
        resultDetail.set연구개발비_평균(STEP04);

        // STEP05 ------------------------------------------------------------------------------------------------------
        // (순부채)
        String netDebt = CalUtil.sub(totalDebt, cash);
        final String STEP05 = netDebt;
        resultDetail.set순부채(netDebt);

        // 계산
        String rst01 = CalUtil.add(STEP01, STEP02);
        String rst02 = CalUtil.add(rst01, STEP03);
        String rst03 = CalUtil.add(rst02, STEP04);
        String rst04 = CalUtil.sub(rst03, STEP05);
        String result = CalUtil.divide(rst04, issuedShares, RoundingMode.HALF_EVEN);

        return result;
    }

    /**
     * 한 주당 가치를 계산한다. V3
     *
     * @param req
     * @return
     */
    public String calPerValueV3(CompanySharePriceCalculator req, CompanySharePriceResultDetail resultDetail) {

        if(log.isDebugEnabled()) log.debug("CompanySharePriceCalculator = {}", req);

        final String per = req.getPer();                                    // PER
        final String incomGrowth = req.getOperatingIncomeGrowth();          // 영업이익 성장율
        final String epsGrowth = req.getEpsgrowth();                        // EPS 성장율
        final String assetsTotal = req.getCurrentAssetsTotal();             // 유동자산합계
        final String liabilitiesTotal = req.getCurrentLiabilitiesTotal();   // 유동부채합계
        final String intangibleAssets = req.getIntangibleAssets();          // 무형자산
        final String totalDebt = req.getTotalDebt();                        // 총부채
        final String cash = req.getCashAndCashEquivalents();                // 현금성 자산
        final String currentRatio = req.getCurrentRatio();                  // 유동비율
        final String investmentAssets = req.getInvestmentAssets();          // 투자자산 (비유동자산 내)
        final String issuedShares = req.getIssuedShares();                  // 발행주식수

        final String K = StringUtil.defaultString(getLiabilityFactor(Double.parseDouble(currentRatio)));    // 유동부채 차감 비율

        // STEP01 ------------------------------------------------------------------------------------------------------
        // (영업이익 3년 평균 × 성장률 보정 PER)
        // 1. 영업이익 3년 평균 계산
        final String operatingProfitAvg = calOperatingProfitAvg(
                req.getOperatingProfitPrePre(),
                req.getOperatingProfitPre(),
                req.getOperatingProfitCurrent()
        );

        // 2. 성장률 보정 PER
        // PER에 성장률(g)을 단순 가산하여 적정 PER을 보정
        // (×(1+g))은 보수적이면서 실제 시장 밸류에 근접
        String adjustedPER;
        String STEP01;

        BigDecimal perVal = new BigDecimal(per);
        if (perVal.signum() <= 0 || perVal.compareTo(new BigDecimal("100")) > 0) {
            // PER 0 미만이면 적자기업으로 자산가치로만 평가
            // PER이 100 정도가 넘으면 정상적으로 해석 불가한 상황으로(적자 직전, 실적 급감 구간, 초기 성장주) 의미없는 데이터로 판단

            BigDecimal revenueGrowthVal = new BigDecimal(req.getRevenueGrowth());

            if(perVal.signum() <= 0) {
                resultDetail.set적자기업(true);
            }

            // 조건: 매출성장률 > 20% (0.2)
            if (revenueGrowthVal.compareTo(new BigDecimal("0.2")) > 0) {
                // PSR 기반 계산

                // 매출액
                String revenue = req.getRevenue();

                String psr = req.getPsr();
                BigDecimal psrVal = new BigDecimal(psr);
                BigDecimal maxPsr = new BigDecimal("10");   // PSR상한 (20~30인 기업(SaaS 등)이 있으면 과대평가 위험)
                if(psrVal.compareTo(maxPsr) > 0) {
                    psr = maxPsr.toPlainString();
                }

                String growthFactor = getRevenueGrowthFactor(revenueGrowthVal); // 보정계수

                adjustedPER = "0";  // PER 기반 아니므로
                STEP01 = CalUtil.multi(CalUtil.multi(revenue, psr), growthFactor);

                resultDetail.set매출기반평가(true);
                resultDetail.set매출액(revenue);
                resultDetail.setPSR(psr);
                resultDetail.set매출성장률(revenueGrowthVal.toPlainString());
                resultDetail.set매출성장률보정계수(growthFactor);

            } else {
                // 자산가치만 계산
                // 아래 플래그면 "자산가치 기준으로만 평가됨"을 안내
                // 자산가치로만 계산하면 성장 잠재력 높은 적자기업 은 값이 왜곡됨 (예: 테슬라 초기, 아마존 초기, 쿠팡 등)
                adjustedPER = "0";
                STEP01 = "0";
                resultDetail.set수익가치계산불가(true);
            }

        } else {
            // 전기/당기 영업이익으로 흑자전환 여부 판단
            BigDecimal preProfitVal = new BigDecimal(req.getOperatingProfitPre());
            BigDecimal curProfitVal = new BigDecimal(req.getOperatingProfitCurrent());
            BigDecimal g = new BigDecimal(incomGrowth);

            if (preProfitVal.signum() < 0 && curProfitVal.signum() > 0) {
                // 흑자전환 기업: 성장률 대신 보수적 계수 적용 (30% 프리미엄)
                adjustedPER = CalUtil.multi(per, "1.3");
                resultDetail.set흑자전환기업(true);
            } else if (preProfitVal.signum() < 0 && curProfitVal.signum() < 0) {
                // 연속 적자: 성장률 보정 안함
                adjustedPER = per;
            } else {
                // 정상 케이스: 기존 공식 + 성장률 상한 100% 제한 (성장률 100% 이상은 일시적 현상 유지 불가하다 판단)
                BigDecimal gCapped = g.min(new BigDecimal("1.0"));
                adjustedPER = CalUtil.multi(per, CalUtil.add("1", gCapped.toPlainString()));
            }

            // 최종 안전장치: adjustedPER 상한 (PER의 2.5배)
            BigDecimal adjustedPERVal = new BigDecimal(adjustedPER);
            BigDecimal maxAdjustedPER = perVal.multiply(new BigDecimal("2.5"));
            if (adjustedPERVal.compareTo(maxAdjustedPER) > 0) {
                adjustedPER = maxAdjustedPER.setScale(4, RoundingMode.HALF_UP).toPlainString();
            }

            STEP01 = CalUtil.multi(operatingProfitAvg, adjustedPER);
        }

        resultDetail.setPER(per);
        resultDetail.set영업이익성장률(incomGrowth);
        resultDetail.set성장률보정PER(adjustedPER);
        resultDetail.setPEG(calPeg(per, epsGrowth));  // PEG 는 영업이익 성장률이 아닌 EPS 성장률

        // STEP02 ------------------------------------------------------------------------------------------------------
        // (유동자산 − (유동부채 × 비율) + 투자자산)
        final String STEP02 = CalUtil.add(CalUtil.sub(assetsTotal, CalUtil.multi(liabilitiesTotal, K)), investmentAssets);

        // STEP03 ------------------------------------------------------------------------------------------------------
        // (무형자산 × 30%)
        final String STEP03 = CalUtil.multi(intangibleAssets, "0.3");

        // STEP04 (계산에서 제외됨) ------------------------------------------------------------------------------------------------------
        // (최근 3년 평균 R&D)
        resultDetail.set연구개발비_평균(calRnDAvg(req.getRndPrePre(), req.getRndPre(), req.getRndCurrent()));

        // STEP05 ------------------------------------------------------------------------------------------------------
        // (순부채)
        String netDebt = CalUtil.sub(totalDebt, cash);
        final String STEP05 = netDebt;
        resultDetail.set순부채(netDebt);

        // 계산
        String rst01 = CalUtil.add(STEP01, STEP02);
        String rst02 = CalUtil.add(rst01, STEP03);
        String rst04 = CalUtil.sub(rst02, STEP05);
        String result = CalUtil.divide(rst04, issuedShares, RoundingMode.HALF_EVEN);

        return result;
    }

    /**
     * 한 주당 가치를 계산한다. V4 (섹터별 차별화)
     *
     * @param req 계산 파라미터
     * @param resultDetail 결과 상세
     * @param sector 섹터명
     * @return 주당가치
     */
    public String calPerValueV4(CompanySharePriceCalculator req, CompanySharePriceResultDetail resultDetail, String sector) {

        if(log.isDebugEnabled()) log.debug("[V4] CompanySharePriceCalculator = {}", req);
        if(log.isDebugEnabled()) log.debug("[V4] Sector = {}", sector);

        // 섹터별 파라미터 조회
        SectorCalculationParameters sectorParams =
            SectorParameterFactory.getParameters(sector);

        final String per = req.getPer();                                    // PER
        final String incomGrowth = req.getOperatingIncomeGrowth();          // 영업이익 성장율
        final String epsGrowth = req.getEpsgrowth();                        // EPS 성장율
        final String assetsTotal = req.getCurrentAssetsTotal();             // 유동자산합계
        final String liabilitiesTotal = req.getCurrentLiabilitiesTotal();   // 유동부채합계
        final String intangibleAssets = req.getIntangibleAssets();          // 무형자산
        final String totalDebt = req.getTotalDebt();                        // 총부채
        final String cash = req.getCashAndCashEquivalents();                // 현금성 자산
        final String currentRatio = req.getCurrentRatio();                  // 유동비율
        final String investmentAssets = req.getInvestmentAssets();          // 투자자산 (비유동자산 내)
        final String issuedShares = req.getIssuedShares();                  // 발행주식수

        // 유동부채 차감 비율 (섹터별 적용 여부 반영)
        final String K;
        if (sectorParams.isApplyCurrentRatio()) {
            K = StringUtil.defaultString(getLiabilityFactor(Double.parseDouble(currentRatio)));
        } else {
            K = "0";  // 금융업 등은 유동비율 적용 안함
        }

        // STEP01 ------------------------------------------------------------------------------------------------------
        // (영업이익 3년 평균 × 성장률 보정 PER)
        final String operatingProfitAvg = calOperatingProfitAvg(
                req.getOperatingProfitPrePre(),
                req.getOperatingProfitPre(),
                req.getOperatingProfitCurrent()
        );

        String adjustedPER;
        String STEP01;

        BigDecimal perVal = new BigDecimal(per);
        if (perVal.signum() <= 0 || perVal.compareTo(new BigDecimal("100")) > 0) {
            // PER 이상 또는 적자기업 → 매출 기반 평가

            BigDecimal revenueGrowthVal = new BigDecimal(req.getRevenueGrowth());

            if(perVal.signum() <= 0) {
                resultDetail.set적자기업(true);
            }

            // 조건: 매출성장률 > 20% (0.2)
            if (revenueGrowthVal.compareTo(new BigDecimal("0.2")) > 0) {
                // PSR 기반 계산

                String revenue = req.getRevenue();
                String psr = req.getPsr();
                BigDecimal psrVal = new BigDecimal(psr);

                // 섹터별 PSR 상한 적용
                BigDecimal maxPsr = sectorParams.getMaxPSR();
                if(psrVal.compareTo(maxPsr) > 0) {
                    psr = maxPsr.toPlainString();
                }

                String growthFactor = getRevenueGrowthFactor(revenueGrowthVal);

                adjustedPER = "0";
                STEP01 = CalUtil.multi(CalUtil.multi(revenue, psr), growthFactor);

                resultDetail.set매출기반평가(true);
                resultDetail.set매출액(revenue);
                resultDetail.setPSR(psr);
                resultDetail.set매출성장률(revenueGrowthVal.toPlainString());
                resultDetail.set매출성장률보정계수(growthFactor);

            } else {
                // 자산가치만 계산
                adjustedPER = "0";
                STEP01 = "0";
                resultDetail.set수익가치계산불가(true);
            }

        } else {
            // 정상 PER 범위 → 영업이익 기반 평가

            BigDecimal preProfitVal = new BigDecimal(req.getOperatingProfitPre());
            BigDecimal curProfitVal = new BigDecimal(req.getOperatingProfitCurrent());
            BigDecimal g = new BigDecimal(incomGrowth);

            if (preProfitVal.signum() < 0 && curProfitVal.signum() > 0) {
                // 흑자전환 기업: 섹터 기준 PER + 30% 프리미엄
                BigDecimal sectorPER = sectorParams.getBasePER();
                adjustedPER = sectorPER.multiply(new BigDecimal("1.3")).toPlainString();
                resultDetail.set흑자전환기업(true);

            } else if (preProfitVal.signum() < 0 && curProfitVal.signum() < 0) {
                // 연속 적자: 섹터 기준 PER만 적용
                adjustedPER = sectorParams.getBasePER().toPlainString();

            } else {
                // 정상 케이스: 섹터별 성장률 상한 적용
                BigDecimal gCapped = g.min(sectorParams.getGrowthRateCap());
                BigDecimal sectorPER = sectorParams.getBasePER();
                adjustedPER = CalUtil.multi(sectorPER.toPlainString(), CalUtil.add("1", gCapped.toPlainString()));
            }

            // 최종 안전장치: adjustedPER 상한 (섹터 기준 PER의 2.5배)
            BigDecimal adjustedPERVal = new BigDecimal(adjustedPER);
            BigDecimal maxAdjustedPER = sectorParams.getBasePER().multiply(new BigDecimal("2.5"));
            if (adjustedPERVal.compareTo(maxAdjustedPER) > 0) {
                adjustedPER = maxAdjustedPER.setScale(4, RoundingMode.HALF_UP).toPlainString();
            }

            STEP01 = CalUtil.multi(operatingProfitAvg, adjustedPER);
        }

        resultDetail.setPER(per);
        resultDetail.set영업이익성장률(incomGrowth);
        resultDetail.set성장률보정PER(adjustedPER);
        resultDetail.setPEG(calPeg(per, epsGrowth));

        // STEP02 ------------------------------------------------------------------------------------------------------
        // (유동자산 − (유동부채 × 비율) + 투자자산)
        final String STEP02 = CalUtil.add(CalUtil.sub(assetsTotal, CalUtil.multi(liabilitiesTotal, K)), investmentAssets);

        // STEP03 ------------------------------------------------------------------------------------------------------
        // (무형자산 × 섹터별 가중치)
        BigDecimal intangibleWeight = sectorParams.getIntangibleAssetWeight();
        final String STEP03 = CalUtil.multi(intangibleAssets, intangibleWeight.toPlainString());

        // STEP04 (계산에서 제외됨) ------------------------------------------------------------------------------------------------------
        // (최근 3년 평균 R&D) - 참고용
        String rndAvg = calRnDAvg(req.getRndPrePre(), req.getRndPre(), req.getRndCurrent());
        resultDetail.set연구개발비_평균(rndAvg);

        // STEP05 ------------------------------------------------------------------------------------------------------
        // (순부채)
        String netDebt = CalUtil.sub(totalDebt, cash);
        final String STEP05 = netDebt;
        resultDetail.set순부채(netDebt);

        // 계산
        String rst01 = CalUtil.add(STEP01, STEP02);
        String rst02 = CalUtil.add(rst01, STEP03);
        String rst04 = CalUtil.sub(rst02, STEP05);
        String result = CalUtil.divide(rst04, issuedShares, RoundingMode.HALF_EVEN);

        if(log.isDebugEnabled()) {
            log.debug("[V4 계산] 섹터:{}, STEP01:{}, STEP02:{}, STEP03:{}, STEP05:{}, 결과:{}",
                sector, STEP01, STEP02, STEP03, STEP05, result);
        }

        return result;
    }

    /**
     * <pre>
     * V5
     * 계산방식은 V4와 같음(구별을 위해 분리)
     * </pre>
     * @param req
     * @param resultDetail
     * @param sector
     * @return
     */
    public String calPerValueV5(CompanySharePriceCalculator req, CompanySharePriceResultDetail resultDetail, String sector) {

        if(log.isDebugEnabled()) log.debug("[V4] CompanySharePriceCalculator = {}", req);
        if(log.isDebugEnabled()) log.debug("[V4] Sector = {}", sector);

        // 섹터별 파라미터 조회
        SectorCalculationParameters sectorParams =
                SectorParameterFactory.getParameters(sector);

        final String per = req.getPer();                                    // PER
        final String incomGrowth = req.getOperatingIncomeGrowth();          // 영업이익 성장율
        final String epsGrowth = req.getEpsgrowth();                        // EPS 성장율
        final String assetsTotal = req.getCurrentAssetsTotal();             // 유동자산합계
        final String liabilitiesTotal = req.getCurrentLiabilitiesTotal();   // 유동부채합계
        final String intangibleAssets = req.getIntangibleAssets();          // 무형자산
        final String totalDebt = req.getTotalDebt();                        // 총부채
        final String cash = req.getCashAndCashEquivalents();                // 현금성 자산
        final String currentRatio = req.getCurrentRatio();                  // 유동비율
        final String investmentAssets = req.getInvestmentAssets();          // 투자자산 (비유동자산 내)
        final String issuedShares = req.getIssuedShares();                  // 발행주식수

        // 유동부채 차감 비율 (섹터별 적용 여부 반영)
        final String K;
        if (sectorParams.isApplyCurrentRatio()) {
            K = StringUtil.defaultString(getLiabilityFactor(Double.parseDouble(currentRatio)));
        } else {
            K = "0";  // 금융업 등은 유동비율 적용 안함
        }

        // STEP01 ------------------------------------------------------------------------------------------------------
        // (영업이익 3년 평균 × 성장률 보정 PER)
        final String operatingProfitAvg = calOperatingProfitAvg(
                req.getOperatingProfitPrePre(),
                req.getOperatingProfitPre(),
                req.getOperatingProfitCurrent()
        );

        String adjustedPER;
        String STEP01;

        BigDecimal perVal = new BigDecimal(per);
        if (perVal.signum() <= 0 || perVal.compareTo(new BigDecimal("100")) > 0) {
            // PER 이상 또는 적자기업 → 매출 기반 평가

            BigDecimal revenueGrowthVal = new BigDecimal(req.getRevenueGrowth());

            if(perVal.signum() <= 0) {
                resultDetail.set적자기업(true);
            }

            // 조건: 매출성장률 > 20% (0.2)
            if (revenueGrowthVal.compareTo(new BigDecimal("0.2")) > 0) {
                // PSR 기반 계산

                String revenue = req.getRevenue();
                String psr = req.getPsr();
                BigDecimal psrVal = new BigDecimal(psr);

                // 섹터별 PSR 상한 적용
                BigDecimal maxPsr = sectorParams.getMaxPSR();
                if(psrVal.compareTo(maxPsr) > 0) {
                    psr = maxPsr.toPlainString();
                }

                String growthFactor = getRevenueGrowthFactor(revenueGrowthVal);

                adjustedPER = "0";
                STEP01 = CalUtil.multi(CalUtil.multi(revenue, psr), growthFactor);

                resultDetail.set매출기반평가(true);
                resultDetail.set매출액(revenue);
                resultDetail.setPSR(psr);
                resultDetail.set매출성장률(revenueGrowthVal.toPlainString());
                resultDetail.set매출성장률보정계수(growthFactor);

            } else {
                // 자산가치만 계산
                adjustedPER = "0";
                STEP01 = "0";
                resultDetail.set수익가치계산불가(true);
            }

        } else {
            // 정상 PER 범위 → 영업이익 기반 평가

            BigDecimal preProfitVal = new BigDecimal(req.getOperatingProfitPre());
            BigDecimal curProfitVal = new BigDecimal(req.getOperatingProfitCurrent());
            BigDecimal g = new BigDecimal(incomGrowth);

            if (preProfitVal.signum() < 0 && curProfitVal.signum() > 0) {
                // 흑자전환 기업: 섹터 기준 PER + 30% 프리미엄
                BigDecimal sectorPER = sectorParams.getBasePER();
                adjustedPER = sectorPER.multiply(new BigDecimal("1.3")).toPlainString();
                resultDetail.set흑자전환기업(true);

            } else if (preProfitVal.signum() < 0 && curProfitVal.signum() < 0) {
                // 연속 적자: 섹터 기준 PER만 적용
                adjustedPER = sectorParams.getBasePER().toPlainString();

            } else {
                // 정상 케이스: 섹터별 성장률 상한 적용
                BigDecimal gCapped = g.min(sectorParams.getGrowthRateCap());
                BigDecimal sectorPER = sectorParams.getBasePER();
                adjustedPER = CalUtil.multi(sectorPER.toPlainString(), CalUtil.add("1", gCapped.toPlainString()));
            }

            // 최종 안전장치: adjustedPER 상한 (섹터 기준 PER의 2.5배)
            BigDecimal adjustedPERVal = new BigDecimal(adjustedPER);
            BigDecimal maxAdjustedPER = sectorParams.getBasePER().multiply(new BigDecimal("2.5"));
            if (adjustedPERVal.compareTo(maxAdjustedPER) > 0) {
                adjustedPER = maxAdjustedPER.setScale(4, RoundingMode.HALF_UP).toPlainString();
            }

            STEP01 = CalUtil.multi(operatingProfitAvg, adjustedPER);
        }

        resultDetail.setPER(per);
        resultDetail.set영업이익성장률(incomGrowth);
        resultDetail.set성장률보정PER(adjustedPER);
        resultDetail.setPEG(calPeg(per, epsGrowth));

        // STEP02 ------------------------------------------------------------------------------------------------------
        // (유동자산 − (유동부채 × 비율) + 투자자산)
        final String STEP02 = CalUtil.add(CalUtil.sub(assetsTotal, CalUtil.multi(liabilitiesTotal, K)), investmentAssets);

        // STEP03 ------------------------------------------------------------------------------------------------------
        // (무형자산 × 섹터별 가중치)
        BigDecimal intangibleWeight = sectorParams.getIntangibleAssetWeight();
        final String STEP03 = CalUtil.multi(intangibleAssets, intangibleWeight.toPlainString());

        // STEP04 (계산에서 제외됨) ------------------------------------------------------------------------------------------------------
        // (최근 3년 평균 R&D) - 참고용
        String rndAvg = calRnDAvg(req.getRndPrePre(), req.getRndPre(), req.getRndCurrent());
        resultDetail.set연구개발비_평균(rndAvg);

        // STEP05 ------------------------------------------------------------------------------------------------------
        // (순부채)
        String netDebt = CalUtil.sub(totalDebt, cash);
        final String STEP05 = netDebt;
        resultDetail.set순부채(netDebt);

        // 계산
        String rst01 = CalUtil.add(STEP01, STEP02);
        String rst02 = CalUtil.add(rst01, STEP03);
        String rst04 = CalUtil.sub(rst02, STEP05);
        String result = CalUtil.divide(rst04, issuedShares, RoundingMode.HALF_EVEN);

        if(log.isDebugEnabled()) {
            log.debug("[V4 계산] 섹터:{}, STEP01:{}, STEP02:{}, STEP03:{}, STEP05:{}, 결과:{}",
                    sector, STEP01, STEP02, STEP03, STEP05, result);
        }

        return result;
    }

    /**
     * <pre>
     * V6: 주당가치 계산 개선
     * - 개선1: 실제PER × 0.6 + 섹터PER × 0.4 블렌딩
     * - 개선2: 분기 추세 할인 (STEP01 계산 후 적용)
     * - 개선4: 영업이익 연간 추세 할인 (STEP01 계산 전 적용)
     * </pre>
     */
    public String calPerValueV6(CompanySharePriceCalculator req, CompanySharePriceResultDetail resultDetail, String sector) {

        if(log.isDebugEnabled()) log.debug("[V6] CompanySharePriceCalculator = {}", req);
        if(log.isDebugEnabled()) log.debug("[V6] Sector = {}", sector);

        // 섹터별 파라미터 조회
        SectorCalculationParameters sectorParams =
                SectorParameterFactory.getParameters(sector);

        final String per = req.getPer();                                    // PER
        final String incomGrowth = req.getOperatingIncomeGrowth();          // 영업이익 성장율
        final String epsGrowth = req.getEpsgrowth();                        // EPS 성장율
        final String assetsTotal = req.getCurrentAssetsTotal();             // 유동자산합계
        final String liabilitiesTotal = req.getCurrentLiabilitiesTotal();   // 유동부채합계
        final String intangibleAssets = req.getIntangibleAssets();          // 무형자산
        final String totalDebt = req.getTotalDebt();                        // 총부채
        final String cash = req.getCashAndCashEquivalents();                // 현금성 자산
        final String currentRatio = req.getCurrentRatio();                  // 유동비율
        final String investmentAssets = req.getInvestmentAssets();          // 투자자산 (비유동자산 내)
        final String issuedShares = req.getIssuedShares();                  // 발행주식수

        // 유동부채 차감 비율 (섹터별 적용 여부 반영)
        final String K;
        if (sectorParams.isApplyCurrentRatio()) {
            K = StringUtil.defaultString(getLiabilityFactor(Double.parseDouble(currentRatio)));
        } else {
            K = "0";  // 금융업 등은 유동비율 적용 안함
        }

        // =====================================================================
        // 개선4: 영업이익 연간 추세 팩터 계산
        // =====================================================================
        BigDecimal annualTrendFactor = BigDecimal.ONE;
        BigDecimal curProfit = new BigDecimal(req.getOperatingProfitCurrent());
        BigDecimal preProfit = new BigDecimal(req.getOperatingProfitPre());
        BigDecimal prePreProfit = new BigDecimal(req.getOperatingProfitPrePre());

        if (curProfit.compareTo(preProfit) < 0 && preProfit.compareTo(prePreProfit) < 0) {
            // 연속 하락: 당기 < 전기 < 전전기
            annualTrendFactor = new BigDecimal("0.8");
            resultDetail.set연속하락추세(true);
        } else if (curProfit.compareTo(preProfit) < 0) {
            // 단일 하락: 당기 < 전기
            annualTrendFactor = new BigDecimal("0.9");
            resultDetail.set단일하락추세(true);
        } else if (curProfit.compareTo(preProfit) > 0 && preProfit.compareTo(prePreProfit) > 0) {
            // 연속 상승: 당기 > 전기 > 전전기
            resultDetail.set연속상승추세(true);
        }
        resultDetail.set영업이익추세팩터(annualTrendFactor.toPlainString());

        // STEP01 ------------------------------------------------------------------------------------------------------
        // (영업이익 3년 평균 × 추세팩터 × 성장률 보정 PER)
        String operatingProfitAvg = calOperatingProfitAvg(
                req.getOperatingProfitPrePre(),
                req.getOperatingProfitPre(),
                req.getOperatingProfitCurrent()
        );

        // 개선4: 영업이익 평균에 연간 추세팩터 적용
        operatingProfitAvg = CalUtil.multi(operatingProfitAvg, annualTrendFactor.toPlainString());

        String adjustedPER;
        String STEP01;

        BigDecimal perVal = new BigDecimal(per);
        BigDecimal sectorPER = sectorParams.getBasePER();

        if (perVal.signum() <= 0 || perVal.compareTo(new BigDecimal("100")) > 0) {
            // PER 이상 또는 적자기업 → 매출 기반 평가

            BigDecimal revenueGrowthVal = new BigDecimal(req.getRevenueGrowth());

            if(perVal.signum() <= 0) {
                resultDetail.set적자기업(true);
            }

            // 조건: 매출성장률 > 20% (0.2)
            if (revenueGrowthVal.compareTo(new BigDecimal("0.2")) > 0) {
                // PSR 기반 계산

                String revenue = req.getRevenue();
                String psr = req.getPsr();
                BigDecimal psrVal = new BigDecimal(psr);

                // 섹터별 PSR 상한 적용
                BigDecimal maxPsr = sectorParams.getMaxPSR();
                if(psrVal.compareTo(maxPsr) > 0) {
                    psr = maxPsr.toPlainString();
                }

                String growthFactor = getRevenueGrowthFactor(revenueGrowthVal);

                adjustedPER = "0";
                STEP01 = CalUtil.multi(CalUtil.multi(revenue, psr), growthFactor);

                resultDetail.set매출기반평가(true);
                resultDetail.set매출액(revenue);
                resultDetail.setPSR(psr);
                resultDetail.set매출성장률(revenueGrowthVal.toPlainString());
                resultDetail.set매출성장률보정계수(growthFactor);

                // V6: 블렌딩PER 미적용 (PSR 경로)
                resultDetail.set실제PER(per);
                resultDetail.set섹터PER(sectorPER.toPlainString());
                resultDetail.set블렌딩PER("N/A (PSR경로)");

            } else {
                // 자산가치만 계산
                adjustedPER = "0";
                STEP01 = "0";
                resultDetail.set수익가치계산불가(true);

                resultDetail.set실제PER(per);
                resultDetail.set섹터PER(sectorPER.toPlainString());
                resultDetail.set블렌딩PER("N/A (자산가치경로)");
            }

        } else {
            // 정상 PER 범위 → 영업이익 기반 평가

            BigDecimal preProfitVal = new BigDecimal(req.getOperatingProfitPre());
            BigDecimal curProfitVal = new BigDecimal(req.getOperatingProfitCurrent());
            BigDecimal g = new BigDecimal(incomGrowth);

            if (preProfitVal.signum() < 0 && curProfitVal.signum() > 0) {
                // 흑자전환 기업
                resultDetail.set흑자전환기업(true);

                // 개선1: 흑자전환이어도 PER이 정상범위면 블렌딩 적용
                BigDecimal blendedPER = perVal.multiply(new BigDecimal("0.6"))
                        .add(sectorPER.multiply(new BigDecimal("0.4")));
                BigDecimal gCapped = g.min(sectorParams.getGrowthRateCap());
                adjustedPER = CalUtil.multi(blendedPER.toPlainString(), CalUtil.add("1", gCapped.toPlainString()));

                resultDetail.set실제PER(per);
                resultDetail.set섹터PER(sectorPER.toPlainString());
                resultDetail.set블렌딩PER(blendedPER.setScale(4, RoundingMode.HALF_UP).toPlainString());

            } else if (preProfitVal.signum() < 0 && curProfitVal.signum() < 0) {
                // 연속 적자: 섹터 기준 PER만 적용 (블렌딩 미적용)
                adjustedPER = sectorPER.toPlainString();

                resultDetail.set실제PER(per);
                resultDetail.set섹터PER(sectorPER.toPlainString());
                resultDetail.set블렌딩PER("N/A (연속적자)");

            } else {
                // 정상 케이스: 개선1 - 실제PER × 0.6 + 섹터PER × 0.4
                BigDecimal gCapped = g.min(sectorParams.getGrowthRateCap());
                BigDecimal blendedPER = perVal.multiply(new BigDecimal("0.6"))
                        .add(sectorPER.multiply(new BigDecimal("0.4")));
                adjustedPER = CalUtil.multi(blendedPER.toPlainString(), CalUtil.add("1", gCapped.toPlainString()));

                resultDetail.set실제PER(per);
                resultDetail.set섹터PER(sectorPER.toPlainString());
                resultDetail.set블렌딩PER(blendedPER.setScale(4, RoundingMode.HALF_UP).toPlainString());
            }

            // 최종 안전장치: adjustedPER 상한 (섹터 기준 PER의 2.5배)
            BigDecimal adjustedPERVal = new BigDecimal(adjustedPER);
            BigDecimal maxAdjustedPER = sectorPER.multiply(new BigDecimal("2.5"));
            if (adjustedPERVal.compareTo(maxAdjustedPER) > 0) {
                adjustedPER = maxAdjustedPER.setScale(4, RoundingMode.HALF_UP).toPlainString();
            }

            STEP01 = CalUtil.multi(operatingProfitAvg, adjustedPER);
        }

        // =====================================================================
        // 개선2: 분기 추세 할인 (STEP01 계산 후 적용)
        // =====================================================================
        BigDecimal quarterlyTrendFactor = BigDecimal.ONE;
        String q1Str = req.getQuarterlyOpIncomeQ1();
        String q2Str = req.getQuarterlyOpIncomeQ2();
        String q3Str = req.getQuarterlyOpIncomeQ3();
        String q4Str = req.getQuarterlyOpIncomeQ4();

        if (!StringUtil.isStringEmpty(q1Str) && !StringUtil.isStringEmpty(q2Str)
                && !StringUtil.isStringEmpty(q3Str) && !StringUtil.isStringEmpty(q4Str)) {

            BigDecimal q1 = new BigDecimal(q1Str);
            BigDecimal q2 = new BigDecimal(q2Str);
            BigDecimal q3 = new BigDecimal(q3Str);
            BigDecimal q4 = new BigDecimal(q4Str);

            BigDecimal recentHalf = q1.add(q2);
            BigDecimal olderHalf = q3.add(q4);

            if (q1.compareTo(q2) < 0 && recentHalf.compareTo(olderHalf) < 0) {
                // 연속 분기 악화: Q1 < Q2 AND (Q1+Q2) < (Q3+Q4)
                quarterlyTrendFactor = new BigDecimal("0.7");
                resultDetail.set분기실적악화(true);
            } else if (q1.compareTo(q2) < 0) {
                // 단일 분기 악화: Q1 < Q2
                quarterlyTrendFactor = new BigDecimal("0.85");
                resultDetail.set분기실적악화(true);
            }

            resultDetail.set분기영업이익_Q1(q1Str);
            resultDetail.set분기영업이익_Q2(q2Str);
            resultDetail.set분기영업이익_Q3(q3Str);
            resultDetail.set분기영업이익_Q4(q4Str);
        }
        resultDetail.set분기추세팩터(quarterlyTrendFactor.toPlainString());

        // STEP01에 분기 추세팩터 적용
        STEP01 = CalUtil.multi(STEP01, quarterlyTrendFactor.toPlainString());

        resultDetail.setPER(per);
        resultDetail.set영업이익성장률(incomGrowth);
        resultDetail.set성장률보정PER(adjustedPER);
        resultDetail.setPEG(calPeg(per, epsGrowth));

        // STEP02 ------------------------------------------------------------------------------------------------------
        // (유동자산 − (유동부채 × 비율) + 투자자산)
        final String STEP02 = CalUtil.add(CalUtil.sub(assetsTotal, CalUtil.multi(liabilitiesTotal, K)), investmentAssets);

        // STEP03 ------------------------------------------------------------------------------------------------------
        // (무형자산 × 섹터별 가중치)
        BigDecimal intangibleWeight = sectorParams.getIntangibleAssetWeight();
        final String STEP03 = CalUtil.multi(intangibleAssets, intangibleWeight.toPlainString());

        // STEP04 (계산에서 제외됨) ------------------------------------------------------------------------------------------------------
        // (최근 3년 평균 R&D) - 참고용
        String rndAvg = calRnDAvg(req.getRndPrePre(), req.getRndPre(), req.getRndCurrent());
        resultDetail.set연구개발비_평균(rndAvg);

        // STEP05 ------------------------------------------------------------------------------------------------------
        // (순부채)
        String netDebt = CalUtil.sub(totalDebt, cash);
        final String STEP05 = netDebt;
        resultDetail.set순부채(netDebt);

        // 계산
        String rst01 = CalUtil.add(STEP01, STEP02);
        String rst02 = CalUtil.add(rst01, STEP03);
        String rst04 = CalUtil.sub(rst02, STEP05);
        String result = CalUtil.divide(rst04, issuedShares, RoundingMode.HALF_EVEN);

        if(log.isDebugEnabled()) {
            log.debug("[V6 계산] 섹터:{}, 연간추세:{}, 분기추세:{}, STEP01:{}, STEP02:{}, STEP03:{}, STEP05:{}, 결과:{}",
                    sector, annualTrendFactor, quarterlyTrendFactor, STEP01, STEP02, STEP03, STEP05, result);
        }

        return result;
    }

    /**
     * 한 주당 가치를 계산한다.
     *
     * @param req 계산에 필요한 데이터가 담긴 StockValueManualReqDTO
     * @return 한 주 가격
     */
    public String calPerValue(CompanySharePriceCalculator req) {

        if(log.isDebugEnabled()) log.debug("CompanySharePriceCalculator = {}", req);

        // 1. 영업이익 평균 계산
        final String operatingProfitAvg = calOperatingProfitAvg(
                req.getOperatingProfitPrePre(),
                req.getOperatingProfitPre(),
                req.getOperatingProfitCurrent()
        );
        if(log.isDebugEnabled()) log.debug("영업이익 평균 = {}", operatingProfitAvg);

        // 요청 객체에서 필요한 값들을 final 변수로 할당
        final String assetsTotal = req.getCurrentAssetsTotal();             // 유동자산합계
        final String liabilitiesTotal = req.getCurrentLiabilitiesTotal();   // 유동부채합계
        final String currentRatio = req.getCurrentRatio();                  // 유동비율
        final String investmentAssets = req.getInvestmentAssets();          // 투자자산 (비유동자산 내)
        final String fixedLiabilities = req.getFixedLiabilities();          // 고정부채 (비유동부채)
        final String issuedShares = req.getIssuedShares();                  // 발행주식수

        // 2. 각 단계별 계산

        // 사업가치: 영업이익 평균 * 10(고정 PER)
        String per = StringUtil.isStringEmpty(req.getPer()) ? "10" : StringUtil.defaultString(req.getPer());
        requestContext.setAttribute(RequestContextConst.PER, per);
        final String businessValue = CalUtil.multi(operatingProfitAvg, per);
        if(log.isDebugEnabled()) log.debug("1. 사업가치 = {}", businessValue);
        requestContext.setAttribute(RequestContextConst.계산_사업가치, businessValue);

        // 재산가치: 유동자산 - (유동부채 * 유동비율) + 투자자산
        final String liabilityProduct = CalUtil.multi(liabilitiesTotal, currentRatio);
        final String assetDifference = CalUtil.sub(assetsTotal, liabilityProduct);
        final String assetValue = CalUtil.add(assetDifference, investmentAssets);
        if(log.isDebugEnabled()) log.debug("2. 재산가치 = {}", assetValue);
        requestContext.setAttribute(RequestContextConst.계산_재산가치, assetValue);

        // 부채: 고정부채 (비유동부채)
        final String debt = fixedLiabilities;
        if(log.isDebugEnabled()) log.debug("3. 부채 = {}", debt);
        requestContext.setAttribute(RequestContextConst.계산_부채, debt);

        // 기업가치: 사업가치 + 재산가치 - 부채
        final String companyValue = CalUtil.sub(CalUtil.add(businessValue, assetValue), debt);
        if(log.isDebugEnabled()) log.debug("4. 기업가치 = {}", companyValue);
        requestContext.setAttribute(RequestContextConst.계산_기업가치, companyValue);

        // 3. 한 주 가격 계산: (기업가치 * 단위복원) / 발행주식수
        return CalUtil.divide(
                CalUtil.multi(companyValue, req.getUnit()),
                issuedShares,
                RoundingMode.HALF_EVEN
        );
    }

    /**
     * 영업이익 평균을 계산한다.
     *
     * @param profitPrePre 전전기 영업이익
     * @param profitPre    전기 영업이익
     * @param profitCurrent 당기 영업이익
     * @return 영업이익 평균 값
     */
    private String calOperatingProfitAvg(String profitPrePre, String profitPre, String profitCurrent) {
        // 전전기 + 전기 + 당기를 한 번에 더한 후 평균 계산
        final String sum = CalUtil.add(CalUtil.add(profitPrePre, profitPre), profitCurrent);
        final String avg = CalUtil.divide(sum, "3", 2, RoundingMode.HALF_UP);

        requestContext.setAttribute(RequestContextConst.영업이익_합계, sum);
        requestContext.setAttribute(RequestContextConst.영업이익_평균, avg);

        return avg;
    }

    /**
     * 연구개발비 평균을 계산한다.
     * @param rndPrePre
     * @param rndPre
     * @param rndCurrent
     * @return
     */
    private String calRnDAvg(String rndPrePre, String rndPre, String rndCurrent) {
        // 전전기 + 전기 + 당기를 한 번에 더한 후 평균 계산
        final String sum = CalUtil.add(CalUtil.add(rndPrePre, rndPre), rndCurrent);
        final String avg = CalUtil.divide(sum, "3", 2, RoundingMode.HALF_UP);

        requestContext.setAttribute(RequestContextConst.연구개발비_합계, sum);
        requestContext.setAttribute(RequestContextConst.연구개발비_평균, avg);

        return avg;
    }

    /**
     * 유동비율(Current Ratio)에 따라 유동부채 차감 비율을 산출하는 메소드
     *
     * 기준:
     * - 유동비율 < 1.0    → 1.0 (전액 차감, 단기 상환 위험 높음)
     * - 1.0 ~ 1.5 미만   → 0.7
     * - 1.5 ~ 2.0 미만   → 0.5
     * - 2.0 이상        → 0.3 (유동성 충분)
     *
     * @param currentRatio 유동비율 (totalCurrentAssets / totalCurrentLiabilities)
     * @return 유동부채 차감 비율 (0.3 ~ 1.0)
     */
    private double getLiabilityFactor(Double currentRatio) {
        if (currentRatio == null) {
            // 데이터 없으면 보수적으로 전액 차감
            return 1.0;
        }

        if (currentRatio < 1.0) {
            return 1.0;
        } else if (currentRatio < 1.5) {
            return 0.7;
        } else if (currentRatio < 2.0) {
            return 0.5;
        } else {
            return 0.3;
        }
    }

    /**
     * <pre>
     * 성장률보정 PER 계산
     * 이론적 성장률 반영 + 현실적 밸류 안정화를 동시에 만족시키기 위한 보정 로직 추가
     * </pre>
     * @param incomGrowth
     * @param per
     * @return
     */
    private String calAdjustedPER(String incomGrowth, String per) {
        if (StringUtil.isStringEmpty(incomGrowth) || StringUtil.isStringEmpty(per)) return null;

        BigDecimal g = new BigDecimal(incomGrowth);

        // 역성장/정체(<=0)면 보정하지 않음: 기본 PER 반환
        if (g.signum() <= 0) return per;

        // 1) λ-보정: 너무 작은 성장률 안정화
        BigDecimal lambda = new BigDecimal("0.05");     // 최소 기대 성장 5%
        BigDecimal gEff = g.add(lambda);                    // g + λ

        // (선택) 상한 캡: 일시적 급등 완화
        BigDecimal gMax = new BigDecimal("0.40");       // 최대 40% 사용
        if (gEff.compareTo(gMax) > 0) gEff = gMax;

        // 2) 이론값: PER ÷ (g + λ)
        String perPureStr = CalUtil.divide(per, gEff.toPlainString(), 4, RoundingMode.HALF_UP);
        BigDecimal perPure = new BigDecimal(perPureStr);

        // 3) 시장 Anchor와 혼합
        BigDecimal anchor = new BigDecimal("25");       // 시장 평균 PER
        BigDecimal alpha  = new BigDecimal("0.4");      // 이론:시장 = 40:60
        BigDecimal adjusted = perPure.multiply(alpha)
                .add(anchor.multiply(BigDecimal.ONE.subtract(alpha)));

        // 4) 최종 캡 (과대 방지)
        BigDecimal cap = new BigDecimal("120");
        if (adjusted.compareTo(cap) > 0) adjusted = cap;

        return adjusted.setScale(4, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * <pre>
     * PEG 계산
     * PEG = PER ÷ EPS성장률 — 1 이하이면 성장 대비 저평가, 1 이상이면 고평가 가능
     * </pre>
     * @param per
     * @param epsGrowth
     * @return
     */
    private String calPeg(String per, String epsGrowth) {

        try {
            if (StringUtil.isStringEmpty(per) || StringUtil.isStringEmpty(epsGrowth)) return null;

            BigDecimal perBd = new BigDecimal(per);
            BigDecimal gBd   = new BigDecimal(epsGrowth); // abs() 쓰지 않음

            // 역성장/정체 → PEG는 의미 없음
            if (gBd.signum() <= 0) return "999";

            // 분모는 "퍼센트". 0.x(비율)로 들어오면 ×100, 이미 퍼센트(>1)이면 그대로
            BigDecimal growthPctBd = (gBd.compareTo(BigDecimal.ONE) > 0)
                    ? gBd
                    : gBd.multiply(new BigDecimal("100"));

//            // 너무 작은 성장률(≈0)은 폭주 방지용 필터 (선택)
//            if (growthPctBd.compareTo(new BigDecimal("0.01")) < 0) return null; // 0.01% 미만이면 N/A

            return perBd.divide(growthPctBd, 4, RoundingMode.HALF_UP).toPlainString();
        } catch (Exception ignore) {
            return null;
        }

        // 역성장 계산방지 로직 없는거(old)
//        String pegValue = null;
//        try {
//            if (!StringUtil.isStringEmpty(per) && !StringUtil.isStringEmpty(epsGrowth)) {
//                java.math.BigDecimal perBd = new java.math.BigDecimal(per);
//                java.math.BigDecimal gBd  = new java.math.BigDecimal(epsGrowth).abs();
//                java.math.BigDecimal growthPctBd = (gBd.compareTo(java.math.BigDecimal.ONE) > 0)
//                        ? gBd
//                        : gBd.multiply(new java.math.BigDecimal("100"));
//                if (growthPctBd.signum() > 0) {
//                    pegValue = perBd.divide(growthPctBd, 4, java.math.RoundingMode.HALF_UP).toPlainString();
//                }
//            }
//        } catch (Exception ignore) {}
//
//        return pegValue;
    }

    /**
     * 매출성장률에 따른 보정계수
     * @param revenueGrowth
     * @return
     */
    private String getRevenueGrowthFactor(BigDecimal revenueGrowth) {
        // 20% 미만: PSR 적용 안함 (기존 자산가치만)
        // 20% ~ 30%: 0.3
        // 30% ~ 50%: 0.5
        // 50% ~ 80%: 0.7
        // 80% 이상: 0.9 (상한)

        if (revenueGrowth.compareTo(new BigDecimal("0.2")) < 0) {
            return null;  // PSR 미적용
        } else if (revenueGrowth.compareTo(new BigDecimal("0.3")) < 0) {
            return "0.3";
        } else if (revenueGrowth.compareTo(new BigDecimal("0.5")) < 0) {
            return "0.5";
        } else if (revenueGrowth.compareTo(new BigDecimal("0.8")) < 0) {
            return "0.7";
        } else {
            return "0.9";  // 상한
        }
    }

}