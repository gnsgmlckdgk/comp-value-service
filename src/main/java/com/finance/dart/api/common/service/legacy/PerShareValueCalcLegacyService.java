package com.finance.dart.api.common.service.legacy;

import com.finance.dart.api.common.dto.CompanySharePriceCalculator;
import com.finance.dart.api.common.dto.CompanySharePriceResultDetail;
import com.finance.dart.api.common.dto.SectorCalculationParameters;
import com.finance.dart.api.common.service.PerShareValueCalcHelper;
import com.finance.dart.api.common.service.SectorParameterFactory;
import com.finance.dart.common.util.CalUtil;
import com.finance.dart.common.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 한 주당 가치 계산 서비스 - Legacy (V2~V7)
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class PerShareValueCalcLegacyService {

    private final PerShareValueCalcHelper calcHelper;

    /**
     * 한 주당 가치를 계산한다. V2
     */
    public String calPerValueV2(CompanySharePriceCalculator req, CompanySharePriceResultDetail resultDetail) {

        if(log.isDebugEnabled()) log.debug("CompanySharePriceCalculator = {}", req);

        final String per = req.getPer();
        final String incomGrowth = req.getOperatingIncomeGrowth();
        final String epsGrowth = req.getEpsgrowth();
        final String assetsTotal = req.getCurrentAssetsTotal();
        final String liabilitiesTotal = req.getCurrentLiabilitiesTotal();
        final String intangibleAssets = req.getIntangibleAssets();
        final String totalDebt = req.getTotalDebt();
        final String cash = req.getCashAndCashEquivalents();
        final String currentRatio = req.getCurrentRatio();
        final String investmentAssets = req.getInvestmentAssets();
        final String issuedShares = req.getIssuedShares();

        final String K = StringUtil.defaultString(calcHelper.getLiabilityFactor(Double.parseDouble(currentRatio)));

        final String operatingProfitAvg = calcHelper.calOperatingProfitAvg(
                req.getOperatingProfitPrePre(),
                req.getOperatingProfitPre(),
                req.getOperatingProfitCurrent()
        );

        String adjustedPER = CalUtil.multi(per, CalUtil.add("1", incomGrowth));

        final String STEP01 = CalUtil.multi(operatingProfitAvg, adjustedPER);

        resultDetail.setPER(per);
        resultDetail.set영업이익성장률(incomGrowth);
        resultDetail.set성장률보정PER(adjustedPER);
        resultDetail.setPEG(calcHelper.calPeg(per, epsGrowth));

        String assets = CalUtil.sub(assetsTotal, cash);
        final String STEP02 = CalUtil.add(CalUtil.sub(assets, CalUtil.multi(liabilitiesTotal, K)), investmentAssets);

        final String STEP03 = CalUtil.multi(intangibleAssets, "0.3");

        final String STEP04 = calcHelper.calRnDAvg(req.getRndPrePre(), req.getRndPre(), req.getRndCurrent());
        resultDetail.set연구개발비_평균(STEP04);

        String netDebt = CalUtil.sub(totalDebt, cash);
        final String STEP05 = netDebt;
        resultDetail.set순부채(netDebt);

        String rst01 = CalUtil.add(STEP01, STEP02);
        String rst02 = CalUtil.add(rst01, STEP03);
        String rst03 = CalUtil.add(rst02, STEP04);
        String rst04 = CalUtil.sub(rst03, STEP05);
        String result = CalUtil.divide(rst04, issuedShares, RoundingMode.HALF_EVEN);

        return result;
    }

    /**
     * 한 주당 가치를 계산한다. V3
     */
    public String calPerValueV3(CompanySharePriceCalculator req, CompanySharePriceResultDetail resultDetail) {

        if(log.isDebugEnabled()) log.debug("CompanySharePriceCalculator = {}", req);

        final String per = req.getPer();
        final String incomGrowth = req.getOperatingIncomeGrowth();
        final String epsGrowth = req.getEpsgrowth();
        final String assetsTotal = req.getCurrentAssetsTotal();
        final String liabilitiesTotal = req.getCurrentLiabilitiesTotal();
        final String intangibleAssets = req.getIntangibleAssets();
        final String totalDebt = req.getTotalDebt();
        final String cash = req.getCashAndCashEquivalents();
        final String currentRatio = req.getCurrentRatio();
        final String investmentAssets = req.getInvestmentAssets();
        final String issuedShares = req.getIssuedShares();

        final String K = StringUtil.defaultString(calcHelper.getLiabilityFactor(Double.parseDouble(currentRatio)));

        final String operatingProfitAvg = calcHelper.calOperatingProfitAvg(
                req.getOperatingProfitPrePre(),
                req.getOperatingProfitPre(),
                req.getOperatingProfitCurrent()
        );

        String adjustedPER;
        String STEP01;

        BigDecimal perVal = new BigDecimal(per);
        if (perVal.signum() <= 0 || perVal.compareTo(new BigDecimal("100")) > 0) {
            BigDecimal revenueGrowthVal = new BigDecimal(req.getRevenueGrowth());

            if(perVal.signum() <= 0) {
                resultDetail.set적자기업(true);
            }

            if (revenueGrowthVal.compareTo(new BigDecimal("0.2")) > 0) {
                String revenue = req.getRevenue();
                String psr = req.getPsr();
                BigDecimal psrVal = new BigDecimal(psr);
                BigDecimal maxPsr = new BigDecimal("10");
                if(psrVal.compareTo(maxPsr) > 0) {
                    psr = maxPsr.toPlainString();
                }

                String growthFactor = calcHelper.getRevenueGrowthFactor(revenueGrowthVal);

                adjustedPER = "0";
                STEP01 = CalUtil.multi(CalUtil.multi(revenue, psr), growthFactor);

                resultDetail.set매출기반평가(true);
                resultDetail.set매출액(revenue);
                resultDetail.setPSR(psr);
                resultDetail.set매출성장률(revenueGrowthVal.toPlainString());
                resultDetail.set매출성장률보정계수(growthFactor);

            } else {
                adjustedPER = "0";
                STEP01 = "0";
                resultDetail.set수익가치계산불가(true);
            }

        } else {
            BigDecimal preProfitVal = new BigDecimal(req.getOperatingProfitPre());
            BigDecimal curProfitVal = new BigDecimal(req.getOperatingProfitCurrent());
            BigDecimal g = new BigDecimal(incomGrowth);

            if (preProfitVal.signum() < 0 && curProfitVal.signum() > 0) {
                adjustedPER = CalUtil.multi(per, "1.3");
                resultDetail.set흑자전환기업(true);
            } else if (preProfitVal.signum() < 0 && curProfitVal.signum() < 0) {
                adjustedPER = per;
            } else {
                BigDecimal gCapped = g.min(new BigDecimal("1.0"));
                adjustedPER = CalUtil.multi(per, CalUtil.add("1", gCapped.toPlainString()));
            }

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
        resultDetail.setPEG(calcHelper.calPeg(per, epsGrowth));

        final String STEP02 = CalUtil.add(CalUtil.sub(assetsTotal, CalUtil.multi(liabilitiesTotal, K)), investmentAssets);

        final String STEP03 = CalUtil.multi(intangibleAssets, "0.3");

        resultDetail.set연구개발비_평균(calcHelper.calRnDAvg(req.getRndPrePre(), req.getRndPre(), req.getRndCurrent()));

        String netDebt = CalUtil.sub(totalDebt, cash);
        final String STEP05 = netDebt;
        resultDetail.set순부채(netDebt);

        String rst01 = CalUtil.add(STEP01, STEP02);
        String rst02 = CalUtil.add(rst01, STEP03);
        String rst04 = CalUtil.sub(rst02, STEP05);
        String result = CalUtil.divide(rst04, issuedShares, RoundingMode.HALF_EVEN);

        return result;
    }

    /**
     * 한 주당 가치를 계산한다. V4 (섹터별 차별화)
     */
    public String calPerValueV4(CompanySharePriceCalculator req, CompanySharePriceResultDetail resultDetail, String sector) {

        if(log.isDebugEnabled()) log.debug("[V4] CompanySharePriceCalculator = {}", req);
        if(log.isDebugEnabled()) log.debug("[V4] Sector = {}", sector);

        SectorCalculationParameters sectorParams = SectorParameterFactory.getParameters(sector);

        final String per = req.getPer();
        final String incomGrowth = req.getOperatingIncomeGrowth();
        final String epsGrowth = req.getEpsgrowth();
        final String assetsTotal = req.getCurrentAssetsTotal();
        final String liabilitiesTotal = req.getCurrentLiabilitiesTotal();
        final String intangibleAssets = req.getIntangibleAssets();
        final String totalDebt = req.getTotalDebt();
        final String cash = req.getCashAndCashEquivalents();
        final String currentRatio = req.getCurrentRatio();
        final String investmentAssets = req.getInvestmentAssets();
        final String issuedShares = req.getIssuedShares();

        final String K;
        if (sectorParams.isApplyCurrentRatio()) {
            K = StringUtil.defaultString(calcHelper.getLiabilityFactor(Double.parseDouble(currentRatio)));
        } else {
            K = "0";
        }

        final String operatingProfitAvg = calcHelper.calOperatingProfitAvg(
                req.getOperatingProfitPrePre(), req.getOperatingProfitPre(), req.getOperatingProfitCurrent());

        String adjustedPER;
        String STEP01;

        BigDecimal perVal = new BigDecimal(per);
        if (perVal.signum() <= 0 || perVal.compareTo(new BigDecimal("100")) > 0) {
            BigDecimal revenueGrowthVal = new BigDecimal(req.getRevenueGrowth());

            if(perVal.signum() <= 0) resultDetail.set적자기업(true);

            if (revenueGrowthVal.compareTo(new BigDecimal("0.2")) > 0) {
                String revenue = req.getRevenue();
                String psr = req.getPsr();
                BigDecimal psrVal = new BigDecimal(psr);
                BigDecimal maxPsr = sectorParams.getMaxPSR();
                if(psrVal.compareTo(maxPsr) > 0) psr = maxPsr.toPlainString();

                String growthFactor = calcHelper.getRevenueGrowthFactor(revenueGrowthVal);
                adjustedPER = "0";
                STEP01 = CalUtil.multi(CalUtil.multi(revenue, psr), growthFactor);

                resultDetail.set매출기반평가(true);
                resultDetail.set매출액(revenue);
                resultDetail.setPSR(psr);
                resultDetail.set매출성장률(revenueGrowthVal.toPlainString());
                resultDetail.set매출성장률보정계수(growthFactor);
            } else {
                adjustedPER = "0";
                STEP01 = "0";
                resultDetail.set수익가치계산불가(true);
            }
        } else {
            BigDecimal preProfitVal = new BigDecimal(req.getOperatingProfitPre());
            BigDecimal curProfitVal = new BigDecimal(req.getOperatingProfitCurrent());
            BigDecimal g = new BigDecimal(incomGrowth);

            if (preProfitVal.signum() < 0 && curProfitVal.signum() > 0) {
                BigDecimal sectorPER = sectorParams.getBasePER();
                adjustedPER = sectorPER.multiply(new BigDecimal("1.3")).toPlainString();
                resultDetail.set흑자전환기업(true);
            } else if (preProfitVal.signum() < 0 && curProfitVal.signum() < 0) {
                adjustedPER = sectorParams.getBasePER().toPlainString();
            } else {
                BigDecimal gCapped = g.min(sectorParams.getGrowthRateCap());
                BigDecimal sectorPER = sectorParams.getBasePER();
                adjustedPER = CalUtil.multi(sectorPER.toPlainString(), CalUtil.add("1", gCapped.toPlainString()));
            }

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
        resultDetail.setPEG(calcHelper.calPeg(per, epsGrowth));

        final String STEP02 = CalUtil.add(CalUtil.sub(assetsTotal, CalUtil.multi(liabilitiesTotal, K)), investmentAssets);

        BigDecimal intangibleWeight = sectorParams.getIntangibleAssetWeight();
        final String STEP03 = CalUtil.multi(intangibleAssets, intangibleWeight.toPlainString());

        String rndAvg = calcHelper.calRnDAvg(req.getRndPrePre(), req.getRndPre(), req.getRndCurrent());
        resultDetail.set연구개발비_평균(rndAvg);

        String netDebt = CalUtil.sub(totalDebt, cash);
        final String STEP05 = netDebt;
        resultDetail.set순부채(netDebt);

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
     * V5 (계산방식은 V4와 동일)
     */
    public String calPerValueV5(CompanySharePriceCalculator req, CompanySharePriceResultDetail resultDetail, String sector) {
        return calPerValueV4(req, resultDetail, sector);
    }

    /**
     * V6: 주당가치 계산 개선
     */
    public String calPerValueV6(CompanySharePriceCalculator req, CompanySharePriceResultDetail resultDetail, String sector) {

        if(log.isDebugEnabled()) log.debug("[V6] CompanySharePriceCalculator = {}", req);
        if(log.isDebugEnabled()) log.debug("[V6] Sector = {}", sector);

        SectorCalculationParameters sectorParams = SectorParameterFactory.getParameters(sector);

        final String per = req.getPer();
        final String incomGrowth = req.getOperatingIncomeGrowth();
        final String epsGrowth = req.getEpsgrowth();
        final String assetsTotal = req.getCurrentAssetsTotal();
        final String liabilitiesTotal = req.getCurrentLiabilitiesTotal();
        final String intangibleAssets = req.getIntangibleAssets();
        final String totalDebt = req.getTotalDebt();
        final String cash = req.getCashAndCashEquivalents();
        final String currentRatio = req.getCurrentRatio();
        final String investmentAssets = req.getInvestmentAssets();
        final String issuedShares = req.getIssuedShares();

        final String K;
        if (sectorParams.isApplyCurrentRatio()) {
            K = StringUtil.defaultString(calcHelper.getLiabilityFactor(Double.parseDouble(currentRatio)));
        } else {
            K = "0";
        }

        // 영업이익 연간 추세 팩터 계산
        BigDecimal annualTrendFactor = BigDecimal.ONE;
        BigDecimal curProfit = new BigDecimal(req.getOperatingProfitCurrent());
        BigDecimal preProfit = new BigDecimal(req.getOperatingProfitPre());
        BigDecimal prePreProfit = new BigDecimal(req.getOperatingProfitPrePre());

        if (curProfit.compareTo(preProfit) < 0 && preProfit.compareTo(prePreProfit) < 0) {
            annualTrendFactor = new BigDecimal("0.8");
            resultDetail.set연속하락추세(true);
        } else if (curProfit.compareTo(preProfit) < 0) {
            annualTrendFactor = new BigDecimal("0.9");
            resultDetail.set단일하락추세(true);
        } else if (curProfit.compareTo(preProfit) > 0 && preProfit.compareTo(prePreProfit) > 0) {
            resultDetail.set연속상승추세(true);
        }
        resultDetail.set영업이익추세팩터(annualTrendFactor.toPlainString());

        String operatingProfitAvg = calcHelper.calOperatingProfitAvg(
                req.getOperatingProfitPrePre(), req.getOperatingProfitPre(), req.getOperatingProfitCurrent());
        operatingProfitAvg = CalUtil.multi(operatingProfitAvg, annualTrendFactor.toPlainString());

        String adjustedPER;
        String STEP01;

        BigDecimal perVal = new BigDecimal(per);
        BigDecimal sectorPER = sectorParams.getBasePER();

        if (perVal.signum() <= 0 || perVal.compareTo(new BigDecimal("100")) > 0) {
            BigDecimal revenueGrowthVal = new BigDecimal(req.getRevenueGrowth());
            if(perVal.signum() <= 0) resultDetail.set적자기업(true);

            if (revenueGrowthVal.compareTo(new BigDecimal("0.2")) > 0) {
                String revenue = req.getRevenue();
                String psr = req.getPsr();
                BigDecimal psrVal = new BigDecimal(psr);
                BigDecimal maxPsr = sectorParams.getMaxPSR();
                if(psrVal.compareTo(maxPsr) > 0) psr = maxPsr.toPlainString();

                String growthFactor = calcHelper.getRevenueGrowthFactor(revenueGrowthVal);
                adjustedPER = "0";
                STEP01 = CalUtil.multi(CalUtil.multi(revenue, psr), growthFactor);

                resultDetail.set매출기반평가(true);
                resultDetail.set매출액(revenue);
                resultDetail.setPSR(psr);
                resultDetail.set매출성장률(revenueGrowthVal.toPlainString());
                resultDetail.set매출성장률보정계수(growthFactor);

                resultDetail.set실제PER(per);
                resultDetail.set섹터PER(sectorPER.toPlainString());
                resultDetail.set블렌딩PER("N/A (PSR경로)");
            } else {
                adjustedPER = "0";
                STEP01 = "0";
                resultDetail.set수익가치계산불가(true);
                resultDetail.set실제PER(per);
                resultDetail.set섹터PER(sectorPER.toPlainString());
                resultDetail.set블렌딩PER("N/A (자산가치경로)");
            }
        } else {
            BigDecimal preProfitVal = new BigDecimal(req.getOperatingProfitPre());
            BigDecimal curProfitVal = new BigDecimal(req.getOperatingProfitCurrent());
            BigDecimal g = new BigDecimal(incomGrowth);

            if (preProfitVal.signum() < 0 && curProfitVal.signum() > 0) {
                resultDetail.set흑자전환기업(true);
                BigDecimal blendedPER = perVal.multiply(new BigDecimal("0.6")).add(sectorPER.multiply(new BigDecimal("0.4")));
                BigDecimal gCapped = g.min(sectorParams.getGrowthRateCap());
                adjustedPER = CalUtil.multi(blendedPER.toPlainString(), CalUtil.add("1", gCapped.toPlainString()));
                resultDetail.set실제PER(per);
                resultDetail.set섹터PER(sectorPER.toPlainString());
                resultDetail.set블렌딩PER(blendedPER.setScale(4, RoundingMode.HALF_UP).toPlainString());
            } else if (preProfitVal.signum() < 0 && curProfitVal.signum() < 0) {
                adjustedPER = sectorPER.toPlainString();
                resultDetail.set실제PER(per);
                resultDetail.set섹터PER(sectorPER.toPlainString());
                resultDetail.set블렌딩PER("N/A (연속적자)");
            } else {
                BigDecimal gCapped = g.min(sectorParams.getGrowthRateCap());
                BigDecimal blendedPER = perVal.multiply(new BigDecimal("0.6")).add(sectorPER.multiply(new BigDecimal("0.4")));
                adjustedPER = CalUtil.multi(blendedPER.toPlainString(), CalUtil.add("1", gCapped.toPlainString()));
                resultDetail.set실제PER(per);
                resultDetail.set섹터PER(sectorPER.toPlainString());
                resultDetail.set블렌딩PER(blendedPER.setScale(4, RoundingMode.HALF_UP).toPlainString());
            }

            BigDecimal adjustedPERVal = new BigDecimal(adjustedPER);
            BigDecimal maxAdjustedPER = sectorPER.multiply(new BigDecimal("2.5"));
            if (adjustedPERVal.compareTo(maxAdjustedPER) > 0) {
                adjustedPER = maxAdjustedPER.setScale(4, RoundingMode.HALF_UP).toPlainString();
            }

            STEP01 = CalUtil.multi(operatingProfitAvg, adjustedPER);
        }

        // 분기 추세 할인
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
                quarterlyTrendFactor = new BigDecimal("0.7");
                resultDetail.set분기실적악화(true);
            } else if (q1.compareTo(q2) < 0) {
                quarterlyTrendFactor = new BigDecimal("0.85");
                resultDetail.set분기실적악화(true);
            }

            resultDetail.set분기영업이익_Q1(q1Str);
            resultDetail.set분기영업이익_Q2(q2Str);
            resultDetail.set분기영업이익_Q3(q3Str);
            resultDetail.set분기영업이익_Q4(q4Str);
        }
        resultDetail.set분기추세팩터(quarterlyTrendFactor.toPlainString());
        STEP01 = CalUtil.multi(STEP01, quarterlyTrendFactor.toPlainString());

        resultDetail.setPER(per);
        resultDetail.set영업이익성장률(incomGrowth);
        resultDetail.set성장률보정PER(adjustedPER);
        resultDetail.setPEG(calcHelper.calPeg(per, epsGrowth));

        final String STEP02 = CalUtil.add(CalUtil.sub(assetsTotal, CalUtil.multi(liabilitiesTotal, K)), investmentAssets);

        BigDecimal intangibleWeight = sectorParams.getIntangibleAssetWeight();
        final String STEP03 = CalUtil.multi(intangibleAssets, intangibleWeight.toPlainString());

        String rndAvg = calcHelper.calRnDAvg(req.getRndPrePre(), req.getRndPre(), req.getRndCurrent());
        resultDetail.set연구개발비_평균(rndAvg);

        String netDebt = CalUtil.sub(totalDebt, cash);
        final String STEP05 = netDebt;
        resultDetail.set순부채(netDebt);

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
     * V7: V6 기반 + 가중평균 + PER상한 1.8배 + 분기적자전환 감지
     */
    public String calPerValueV7(CompanySharePriceCalculator req, CompanySharePriceResultDetail resultDetail, String sector) {

        if(log.isDebugEnabled()) log.debug("[V7] CompanySharePriceCalculator = {}", req);
        if(log.isDebugEnabled()) log.debug("[V7] Sector = {}", sector);

        SectorCalculationParameters sectorParams = SectorParameterFactory.getParameters(sector);

        final String per = req.getPer();
        final String incomGrowth = req.getOperatingIncomeGrowth();
        final String epsGrowth = req.getEpsgrowth();
        final String assetsTotal = req.getCurrentAssetsTotal();
        final String liabilitiesTotal = req.getCurrentLiabilitiesTotal();
        final String intangibleAssets = req.getIntangibleAssets();
        final String totalDebt = req.getTotalDebt();
        final String cash = req.getCashAndCashEquivalents();
        final String currentRatio = req.getCurrentRatio();
        final String investmentAssets = req.getInvestmentAssets();
        final String issuedShares = req.getIssuedShares();

        final String K;
        if (sectorParams.isApplyCurrentRatio()) {
            K = StringUtil.defaultString(calcHelper.getLiabilityFactor(Double.parseDouble(currentRatio)));
        } else {
            K = "0";
        }

        // 영업이익 연간 추세 팩터 계산
        BigDecimal annualTrendFactor = BigDecimal.ONE;
        BigDecimal curProfit = new BigDecimal(req.getOperatingProfitCurrent());
        BigDecimal preProfit = new BigDecimal(req.getOperatingProfitPre());
        BigDecimal prePreProfit = new BigDecimal(req.getOperatingProfitPrePre());

        if (curProfit.compareTo(preProfit) < 0 && preProfit.compareTo(prePreProfit) < 0) {
            annualTrendFactor = new BigDecimal("0.8");
            resultDetail.set연속하락추세(true);
        } else if (curProfit.compareTo(preProfit) < 0) {
            annualTrendFactor = new BigDecimal("0.9");
            resultDetail.set단일하락추세(true);
        } else if (curProfit.compareTo(preProfit) > 0 && preProfit.compareTo(prePreProfit) > 0) {
            resultDetail.set연속상승추세(true);
        }
        resultDetail.set영업이익추세팩터(annualTrendFactor.toPlainString());

        // 가중평균 (1:2:3)
        String operatingProfitAvg = calcHelper.calOperatingProfitWeightedAvg(
                req.getOperatingProfitPrePre(), req.getOperatingProfitPre(), req.getOperatingProfitCurrent());
        operatingProfitAvg = CalUtil.multi(operatingProfitAvg, annualTrendFactor.toPlainString());

        String adjustedPER;
        String STEP01;

        BigDecimal perVal = new BigDecimal(per);
        BigDecimal sectorPER = sectorParams.getBasePER();

        if (perVal.signum() <= 0 || perVal.compareTo(new BigDecimal("100")) > 0) {
            BigDecimal revenueGrowthVal = new BigDecimal(req.getRevenueGrowth());
            if(perVal.signum() <= 0) resultDetail.set적자기업(true);

            if (revenueGrowthVal.compareTo(new BigDecimal("0.2")) > 0) {
                String revenue = req.getRevenue();
                String psr = req.getPsr();
                BigDecimal psrVal = new BigDecimal(psr);
                BigDecimal maxPsr = sectorParams.getMaxPSR();
                if(psrVal.compareTo(maxPsr) > 0) psr = maxPsr.toPlainString();

                String growthFactor = calcHelper.getRevenueGrowthFactor(revenueGrowthVal);
                adjustedPER = "0";
                STEP01 = CalUtil.multi(CalUtil.multi(revenue, psr), growthFactor);

                resultDetail.set매출기반평가(true);
                resultDetail.set매출액(revenue);
                resultDetail.setPSR(psr);
                resultDetail.set매출성장률(revenueGrowthVal.toPlainString());
                resultDetail.set매출성장률보정계수(growthFactor);
                resultDetail.set실제PER(per);
                resultDetail.set섹터PER(sectorPER.toPlainString());
                resultDetail.set블렌딩PER("N/A (PSR경로)");
            } else {
                adjustedPER = "0";
                STEP01 = "0";
                resultDetail.set수익가치계산불가(true);
                resultDetail.set실제PER(per);
                resultDetail.set섹터PER(sectorPER.toPlainString());
                resultDetail.set블렌딩PER("N/A (자산가치경로)");
            }
        } else {
            BigDecimal preProfitVal = new BigDecimal(req.getOperatingProfitPre());
            BigDecimal curProfitVal = new BigDecimal(req.getOperatingProfitCurrent());
            BigDecimal g = new BigDecimal(incomGrowth);

            if (preProfitVal.signum() < 0 && curProfitVal.signum() > 0) {
                resultDetail.set흑자전환기업(true);
                BigDecimal blendedPER = perVal.multiply(new BigDecimal("0.6")).add(sectorPER.multiply(new BigDecimal("0.4")));
                BigDecimal gCapped = g.min(sectorParams.getGrowthRateCap());
                adjustedPER = CalUtil.multi(blendedPER.toPlainString(), CalUtil.add("1", gCapped.toPlainString()));
                resultDetail.set실제PER(per);
                resultDetail.set섹터PER(sectorPER.toPlainString());
                resultDetail.set블렌딩PER(blendedPER.setScale(4, RoundingMode.HALF_UP).toPlainString());
            } else if (preProfitVal.signum() < 0 && curProfitVal.signum() < 0) {
                adjustedPER = sectorPER.toPlainString();
                resultDetail.set실제PER(per);
                resultDetail.set섹터PER(sectorPER.toPlainString());
                resultDetail.set블렌딩PER("N/A (연속적자)");
            } else {
                BigDecimal gCapped = g.min(sectorParams.getGrowthRateCap());
                BigDecimal blendedPER = perVal.multiply(new BigDecimal("0.6")).add(sectorPER.multiply(new BigDecimal("0.4")));
                adjustedPER = CalUtil.multi(blendedPER.toPlainString(), CalUtil.add("1", gCapped.toPlainString()));
                resultDetail.set실제PER(per);
                resultDetail.set섹터PER(sectorPER.toPlainString());
                resultDetail.set블렌딩PER(blendedPER.setScale(4, RoundingMode.HALF_UP).toPlainString());
            }

            // PER상한 1.8배
            BigDecimal adjustedPERVal = new BigDecimal(adjustedPER);
            BigDecimal maxAdjustedPER = sectorPER.multiply(new BigDecimal("1.8"));
            if (adjustedPERVal.compareTo(maxAdjustedPER) > 0) {
                adjustedPER = maxAdjustedPER.setScale(4, RoundingMode.HALF_UP).toPlainString();
            }

            STEP01 = CalUtil.multi(operatingProfitAvg, adjustedPER);
        }

        // 분기 추세 할인
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
                quarterlyTrendFactor = new BigDecimal("0.7");
                resultDetail.set분기실적악화(true);
            } else if (q1.compareTo(q2) < 0) {
                quarterlyTrendFactor = new BigDecimal("0.85");
                resultDetail.set분기실적악화(true);
            }

            // 최근 분기 적자전환 감지
            if (q1.signum() < 0 && q2.signum() > 0) {
                quarterlyTrendFactor = quarterlyTrendFactor.multiply(new BigDecimal("0.5"));
                resultDetail.set분기적자전환(true);
            }

            resultDetail.set분기영업이익_Q1(q1Str);
            resultDetail.set분기영업이익_Q2(q2Str);
            resultDetail.set분기영업이익_Q3(q3Str);
            resultDetail.set분기영업이익_Q4(q4Str);
        }
        resultDetail.set분기추세팩터(quarterlyTrendFactor.toPlainString());
        STEP01 = CalUtil.multi(STEP01, quarterlyTrendFactor.toPlainString());

        resultDetail.setPER(per);
        resultDetail.set영업이익성장률(incomGrowth);
        resultDetail.set성장률보정PER(adjustedPER);
        resultDetail.setPEG(calcHelper.calPeg(per, epsGrowth));

        final String STEP02 = CalUtil.add(CalUtil.sub(assetsTotal, CalUtil.multi(liabilitiesTotal, K)), investmentAssets);

        BigDecimal intangibleWeight = sectorParams.getIntangibleAssetWeight();
        final String STEP03 = CalUtil.multi(intangibleAssets, intangibleWeight.toPlainString());

        String rndAvg = calcHelper.calRnDAvg(req.getRndPrePre(), req.getRndPre(), req.getRndCurrent());
        resultDetail.set연구개발비_평균(rndAvg);

        String netDebt = CalUtil.sub(totalDebt, cash);
        final String STEP05 = netDebt;
        resultDetail.set순부채(netDebt);

        String rst01 = CalUtil.add(STEP01, STEP02);
        String rst02 = CalUtil.add(rst01, STEP03);
        String rst04 = CalUtil.sub(rst02, STEP05);
        String result = CalUtil.divide(rst04, issuedShares, RoundingMode.HALF_EVEN);

        if(log.isDebugEnabled()) {
            log.debug("[V7 계산] 섹터:{}, 연간추세:{}, 분기추세:{}, STEP01:{}, STEP02:{}, STEP03:{}, STEP05:{}, 결과:{}",
                    sector, annualTrendFactor, quarterlyTrendFactor, STEP01, STEP02, STEP03, STEP05, result);
        }

        return result;
    }
}
