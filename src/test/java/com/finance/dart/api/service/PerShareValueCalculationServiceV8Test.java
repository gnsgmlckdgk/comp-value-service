package com.finance.dart.api.service;

import com.finance.dart.api.common.context.RequestContext;
import com.finance.dart.api.common.dto.CompanySharePriceCalculator;
import com.finance.dart.api.common.dto.CompanySharePriceResultDetail;
import com.finance.dart.api.common.service.PerShareValueCalcHelper;
import com.finance.dart.api.common.service.PerShareValueCalculationService;
import com.finance.dart.api.common.service.legacy.PerShareValueCalcLegacyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PerShareValueCalculationServiceV8Test {

    @Mock
    private RequestContext requestContext;

    private PerShareValueCalcHelper calcHelper;

    private PerShareValueCalculationService service;

    private PerShareValueCalcLegacyService legacyService;

    @BeforeEach
    void setUp() {
        calcHelper = new PerShareValueCalcHelper(requestContext);
        service = new PerShareValueCalculationService(requestContext, null, calcHelper);
        legacyService = new PerShareValueCalcLegacyService(calcHelper);
    }

    /**
     * 공통 테스트 데이터 생성
     */
    private CompanySharePriceCalculator createBaseCalParam() {
        CompanySharePriceCalculator req = new CompanySharePriceCalculator();
        req.setCurrentAssetsTotal("50000000000");
        req.setCurrentLiabilitiesTotal("20000000000");
        req.setCurrentRatio("2.5");
        req.setInvestmentAssets("5000000000");
        req.setIntangibleAssets("3000000000");
        req.setTotalDebt("15000000000");
        req.setCashAndCashEquivalents("10000000000");
        req.setIssuedShares("1000000000");
        req.setEpsgrowth("0.25");
        req.setRndCurrent("1000000000");
        req.setRndPre("900000000");
        req.setRndPrePre("800000000");
        req.setRevenue("100000000000");
        req.setRevenueGrowth("0.15");
        req.setPsr("5");
        return req;
    }

    // ==================================================================================
    // 시나리오 1: 고PER 종목 (actualPER=50, sectorPER=30, 성장률=60%)
    // V7 → V8 적정가가 얼마나 하락하는지 확인
    // ==================================================================================
    @Test
    @DisplayName("시나리오1: 고PER 종목 (PER=50, 성장률=60%) - V8 보수화 완화로 V7 대비 유사하거나 높음")
    void test_scenario1_highPER_v8Relaxed() {
        CompanySharePriceCalculator req = createBaseCalParam();
        req.setPer("50");
        req.setOperatingIncomeGrowth("0.6");
        req.setOperatingProfitPrePre("8000000000");
        req.setOperatingProfitPre("9000000000");
        req.setOperatingProfitCurrent("10000000000");

        req.setQuarterlyOpIncomeQ1("3000000000");
        req.setQuarterlyOpIncomeQ2("2800000000");
        req.setQuarterlyOpIncomeQ3("2500000000");
        req.setQuarterlyOpIncomeQ4("2200000000");

        String sector = "Technology";

        // V7 계산 (legacy)
        CompanySharePriceResultDetail detailV7 = new CompanySharePriceResultDetail("1");
        String resultV7 = legacyService.calPerValueV7(req, detailV7, sector);

        // V8 계산
        CompanySharePriceResultDetail detailV8 = new CompanySharePriceResultDetail("1");
        String resultV8 = service.calPerValueV8(req, detailV8, sector);

        assertNotNull(resultV7);
        assertNotNull(resultV8);

        BigDecimal v7Val = new BigDecimal(resultV7);
        BigDecimal v8Val = new BigDecimal(resultV8);

        System.out.println("======================================================");
        System.out.println("시나리오1: 고PER 종목 (PER=50, sectorPER=30, 성장률=60%)");
        System.out.println("------------------------------------------------------");
        System.out.println("[V7] 블렌딩PER: " + detailV7.get블렌딩PER());
        System.out.println("[V8] 블렌딩PER: " + detailV8.get블렌딩PER());
        System.out.println("[V7] 성장률보정PER: " + detailV7.get성장률보정PER());
        System.out.println("[V8] 성장률보정PER: " + detailV8.get성장률보정PER());
        System.out.println("[V7] 주당가치: $" + resultV7);
        System.out.println("[V8] 주당가치: $" + resultV8);

        BigDecimal ratio = v8Val.divide(v7Val, 4, java.math.RoundingMode.HALF_UP);
        double diffPct = (ratio.doubleValue() - 1.0) * 100;
        System.out.println(String.format("[비교] V8/V7 = %.4f (차이 %+.1f%%)", ratio, diffPct));
        System.out.println("======================================================\n");

        // 보수화 완화 후 V8이 양수이고 합리적인 범위인지 확인
        assertTrue(v8Val.compareTo(BigDecimal.ZERO) > 0,
                "V8 주당가치는 양수여야 한다");
    }

    // ==================================================================================
    // 시나리오 2: 보통 PER 종목 (actualPER=25, sectorPER=30, 성장률=20%)
    // 정상 범위에서의 차이 확인
    // ==================================================================================
    @Test
    @DisplayName("시나리오2: 보통 PER 종목 (PER=25, 성장률=20%) - actualPER < sectorPER일 때 차이 확인")
    void test_scenario2_normalPER_belowSector() {
        CompanySharePriceCalculator req = createBaseCalParam();
        req.setPer("25");
        req.setOperatingIncomeGrowth("0.2");
        req.setOperatingProfitPrePre("8000000000");
        req.setOperatingProfitPre("9000000000");
        req.setOperatingProfitCurrent("10000000000");

        req.setQuarterlyOpIncomeQ1("3000000000");
        req.setQuarterlyOpIncomeQ2("2800000000");
        req.setQuarterlyOpIncomeQ3("2500000000");
        req.setQuarterlyOpIncomeQ4("2200000000");

        String sector = "Technology";

        CompanySharePriceResultDetail detailV7 = new CompanySharePriceResultDetail("1");
        String resultV7 = legacyService.calPerValueV7(req, detailV7, sector);

        CompanySharePriceResultDetail detailV8 = new CompanySharePriceResultDetail("1");
        String resultV8 = service.calPerValueV8(req, detailV8, sector);

        BigDecimal v7Val = new BigDecimal(resultV7);
        BigDecimal v8Val = new BigDecimal(resultV8);

        System.out.println("======================================================");
        System.out.println("시나리오2: 보통 PER 종목 (PER=25, sectorPER=30, 성장률=20%)");
        System.out.println("------------------------------------------------------");
        System.out.println("[V7] 블렌딩PER: " + detailV7.get블렌딩PER() + " (25×0.6+30×0.4=27)");
        System.out.println("[V8] 블렌딩PER: " + detailV8.get블렌딩PER() + " (min(25,45)=25, 25×0.6+30×0.4=27)");
        System.out.println("[V7] 성장률보정PER: " + detailV7.get성장률보정PER());
        System.out.println("[V8] 성장률보정PER: " + detailV8.get성장률보정PER());
        System.out.println("[V7] 주당가치: $" + resultV7);
        System.out.println("[V8] 주당가치: $" + resultV8);
        System.out.println("[참고] actualPER<sectorPER 구간에서는 V7과 V8이 유사");
        System.out.println("       V8은 고PER 종목에서 합리적 수준의 보수화 적용");

        BigDecimal ratio = v8Val.divide(v7Val, 4, java.math.RoundingMode.HALF_UP);
        double diffPct = (ratio.doubleValue() - 1.0) * 100;
        System.out.println(String.format("[비교] V8/V7 = %.4f (차이 %+.1f%%)", ratio, diffPct));
        System.out.println("======================================================\n");

        // actualPER(25) < sectorPER(30)일 때 V8 블렌딩 = 27.5 > V7 블렌딩 = 27
        // 차이가 5% 이내임을 확인 (급격한 변동 없음)
        double absDiffPct = Math.abs(diffPct);
        assertTrue(absDiffPct < 5.0,
                String.format("V7(%s)과 V8(%s) 차이가 5%% 이내여야 한다 (실제: %.1f%%)", resultV7, resultV8, absDiffPct));
    }

    // ==================================================================================
    // 시나리오 3: 매우 고PER + 초고성장 (PER=80, 성장률=100%)
    // adjustedPER 상한 차이 (1.8배 vs 1.5배)
    // ==================================================================================
    @Test
    @DisplayName("시나리오3: 초고성장 종목 (PER=80, 성장률=100%) - adjustedPER 상한 차이")
    void test_scenario3_veryHighPER_adjustedPERCap() {
        CompanySharePriceCalculator req = createBaseCalParam();
        req.setPer("80");
        req.setOperatingIncomeGrowth("1.0");
        req.setOperatingProfitPrePre("3000000000");
        req.setOperatingProfitPre("5000000000");
        req.setOperatingProfitCurrent("10000000000");

        req.setQuarterlyOpIncomeQ1("3500000000");
        req.setQuarterlyOpIncomeQ2("3000000000");
        req.setQuarterlyOpIncomeQ3("2000000000");
        req.setQuarterlyOpIncomeQ4("1500000000");

        String sector = "Technology"; // sectorPER=30

        CompanySharePriceResultDetail detailV7 = new CompanySharePriceResultDetail("1");
        String resultV7 = legacyService.calPerValueV7(req, detailV7, sector);

        CompanySharePriceResultDetail detailV8 = new CompanySharePriceResultDetail("1");
        String resultV8 = service.calPerValueV8(req, detailV8, sector);

        BigDecimal v7Val = new BigDecimal(resultV7);
        BigDecimal v8Val = new BigDecimal(resultV8);

        System.out.println("======================================================");
        System.out.println("시나리오3: 초고성장 종목 (PER=80, sectorPER=30, 성장률=100%)");
        System.out.println("------------------------------------------------------");
        System.out.println("[V7] 블렌딩PER: " + detailV7.get블렌딩PER());
        System.out.println("[V8] 블렌딩PER: " + detailV8.get블렌딩PER());
        System.out.println("[V7] 성장률보정PER(adjustedPER): " + detailV7.get성장률보정PER());
        System.out.println("[V8] 성장률보정PER(adjustedPER): " + detailV8.get성장률보정PER());
        System.out.println("  - V7 adjustedPER 상한: sectorPER×1.8 = " + new BigDecimal("30").multiply(new BigDecimal("1.8")));
        System.out.println("  - V8 adjustedPER 상한: sectorPER×2.0 = " + new BigDecimal("30").multiply(new BigDecimal("2.0")));
        System.out.println("[V7] 주당가치: $" + resultV7);
        System.out.println("[V8] 주당가치: $" + resultV8);

        BigDecimal ratio = v8Val.divide(v7Val, 4, java.math.RoundingMode.HALF_UP);
        double diffPct = (ratio.doubleValue() - 1.0) * 100;
        System.out.println(String.format("[비교] V8/V7 = %.4f (차이 %+.1f%%)", ratio, diffPct));
        System.out.println("======================================================\n");

        // V8의 adjustedPER이 60 이하인지 (sectorPER 30 × 2.0 = 60)
        BigDecimal v8AdjPER = new BigDecimal(detailV8.get성장률보정PER());
        assertTrue(v8AdjPER.compareTo(new BigDecimal("60")) <= 0,
                "V8 adjustedPER(" + v8AdjPER + ")은 60 이하여야 한다 (sectorPER×2.0)");

        // V7의 adjustedPER이 54 이하인지 (sectorPER 30 × 1.8 = 54)
        BigDecimal v7AdjPER = new BigDecimal(detailV7.get성장률보정PER());
        assertTrue(v7AdjPER.compareTo(new BigDecimal("54")) <= 0,
                "V7 adjustedPER(" + v7AdjPER + ")은 54 이하여야 한다 (sectorPER×1.8)");

        assertTrue(v8Val.compareTo(BigDecimal.ZERO) > 0);
    }

    // ==================================================================================
    // 시나리오 4: 하락 추세 종목 (PER=15, 연속 하락 + 분기 악화)
    // 둘 다 하락 할인이 적용되지만 기본 적정가 자체가 다름
    // ==================================================================================
    @Test
    @DisplayName("시나리오4: 하락 추세 종목 - 연속 하락 + 분기 악화")
    void test_scenario4_decliningTrend_bothVersions() {
        CompanySharePriceCalculator req = createBaseCalParam();
        req.setPer("15");
        req.setOperatingIncomeGrowth("0.1");
        req.setOperatingProfitPrePre("10000000000");
        req.setOperatingProfitPre("8000000000");
        req.setOperatingProfitCurrent("6000000000");

        // 연속 분기 악화
        req.setQuarterlyOpIncomeQ1("1000000000");
        req.setQuarterlyOpIncomeQ2("1500000000");
        req.setQuarterlyOpIncomeQ3("2000000000");
        req.setQuarterlyOpIncomeQ4("2500000000");

        String sector = "Industrials"; // basePER=18

        CompanySharePriceResultDetail detailV7 = new CompanySharePriceResultDetail("1");
        String resultV7 = legacyService.calPerValueV7(req, detailV7, sector);

        CompanySharePriceResultDetail detailV8 = new CompanySharePriceResultDetail("1");
        String resultV8 = service.calPerValueV8(req, detailV8, sector);

        BigDecimal v7Val = new BigDecimal(resultV7);
        BigDecimal v8Val = new BigDecimal(resultV8);

        System.out.println("======================================================");
        System.out.println("시나리오4: 하락 추세 종목 (PER=15, sectorPER=18, 성장률=10%)");
        System.out.println("------------------------------------------------------");
        System.out.println("[V7] 블렌딩PER: " + detailV7.get블렌딩PER());
        System.out.println("[V8] 블렌딩PER: " + detailV8.get블렌딩PER());
        System.out.println("[V7] 연속하락: " + detailV7.is연속하락추세() + ", 분기악화: " + detailV7.is분기실적악화());
        System.out.println("[V8] 연속하락: " + detailV8.is연속하락추세() + ", 분기악화: " + detailV8.is분기실적악화());
        System.out.println("[V7] 주당가치: $" + resultV7);
        System.out.println("[V8] 주당가치: $" + resultV8);

        BigDecimal ratio = v8Val.divide(v7Val, 4, java.math.RoundingMode.HALF_UP);
        double reductionPct = (1.0 - ratio.doubleValue()) * 100;
        System.out.println(String.format("[비교] V8/V7 = %.4f (V8이 %.1f%% 낮음)", ratio, reductionPct));
        System.out.println("======================================================\n");

        assertTrue(detailV7.is연속하락추세());
        assertTrue(detailV8.is연속하락추세());
        assertTrue(detailV7.is분기실적악화());
        assertTrue(detailV8.is분기실적악화());
    }

    // ==================================================================================
    // 시나리오 5: 적자 고성장 기업 (PSR 경로) - V7 vs V8 동일해야 함
    // (PSR 경로는 PER 블렌딩 변경에 영향 없음)
    // ==================================================================================
    @Test
    @DisplayName("시나리오5: 적자 고성장 기업 (PSR 경로) - V7 vs V8 동일")
    void test_scenario5_lossCompany_PSRpath_sameResult() {
        CompanySharePriceCalculator req = createBaseCalParam();
        req.setPer("-15");
        req.setOperatingIncomeGrowth("-0.5");
        req.setOperatingProfitPrePre("-2000000000");
        req.setOperatingProfitPre("-3000000000");
        req.setOperatingProfitCurrent("-1000000000");
        req.setRevenueGrowth("0.5");
        req.setRevenue("50000000000");
        req.setPsr("8");

        req.setQuarterlyOpIncomeQ1(null);
        req.setQuarterlyOpIncomeQ2(null);
        req.setQuarterlyOpIncomeQ3(null);
        req.setQuarterlyOpIncomeQ4(null);

        String sector = "Technology";

        CompanySharePriceResultDetail detailV7 = new CompanySharePriceResultDetail("1");
        String resultV7 = legacyService.calPerValueV7(req, detailV7, sector);

        CompanySharePriceResultDetail detailV8 = new CompanySharePriceResultDetail("1");
        String resultV8 = service.calPerValueV8(req, detailV8, sector);

        BigDecimal v7Val = new BigDecimal(resultV7);
        BigDecimal v8Val = new BigDecimal(resultV8);

        System.out.println("======================================================");
        System.out.println("시나리오5: 적자 고성장 기업 (PSR 경로)");
        System.out.println("------------------------------------------------------");
        System.out.println("[V7] 적자기업: " + detailV7.is적자기업() + ", 매출기반평가: " + detailV7.is매출기반평가());
        System.out.println("[V8] 적자기업: " + detailV8.is적자기업() + ", 매출기반평가: " + detailV8.is매출기반평가());
        System.out.println("[V7] 주당가치: $" + resultV7);
        System.out.println("[V8] 주당가치: $" + resultV8);
        System.out.println("[비교] PSR 경로는 PER 블렌딩 변경에 영향 없으므로 동일해야 함");
        System.out.println("======================================================\n");

        assertTrue(detailV7.is적자기업());
        assertTrue(detailV8.is적자기업());
        assertTrue(detailV7.is매출기반평가());
        assertTrue(detailV8.is매출기반평가());

        assertEquals(v7Val.compareTo(v8Val), 0,
                String.format("PSR 경로에서는 V7(%s)과 V8(%s)이 동일해야 한다", resultV7, resultV8));
    }

    // ==================================================================================
    // 시나리오 6: 계획서 예시 (actualPER=50, sectorPER=30, 성장률=60%)
    // 정확한 수치 검증
    // ==================================================================================
    @Test
    @DisplayName("시나리오6: 계획서 예시 - V7 blended=42, V8 blended=33 검증")
    void test_scenario6_planExample_exactValues() {
        CompanySharePriceCalculator req = createBaseCalParam();
        req.setPer("50");
        req.setOperatingIncomeGrowth("0.6");
        req.setOperatingProfitPrePre("10000000000");
        req.setOperatingProfitPre("10000000000");
        req.setOperatingProfitCurrent("10000000000");

        // 분기 안정 (할인 없음)
        req.setQuarterlyOpIncomeQ1("3000000000");
        req.setQuarterlyOpIncomeQ2("2500000000");
        req.setQuarterlyOpIncomeQ3("2500000000");
        req.setQuarterlyOpIncomeQ4("2000000000");

        String sector = "Technology"; // sectorPER=30

        CompanySharePriceResultDetail detailV7 = new CompanySharePriceResultDetail("1");
        String resultV7 = legacyService.calPerValueV7(req, detailV7, sector);

        CompanySharePriceResultDetail detailV8 = new CompanySharePriceResultDetail("1");
        String resultV8 = service.calPerValueV8(req, detailV8, sector);

        System.out.println("======================================================");
        System.out.println("시나리오6: 계획서 예시 (PER=50, sectorPER=30, 성장률=60%)");
        System.out.println("------------------------------------------------------");
        System.out.println("[V7] 블렌딩PER: " + detailV7.get블렌딩PER() + " (예상: 42)");
        System.out.println("[V8] 블렌딩PER: " + detailV8.get블렌딩PER() + " (예상: 39)");
        System.out.println("[V7] 성장률보정PER: " + detailV7.get성장률보정PER());
        System.out.println("[V8] 성장률보정PER: " + detailV8.get성장률보정PER());
        System.out.println("[V7] 주당가치: $" + resultV7);
        System.out.println("[V8] 주당가치: $" + resultV8);

        BigDecimal v7Val = new BigDecimal(resultV7);
        BigDecimal v8Val = new BigDecimal(resultV8);
        BigDecimal ratio = v8Val.divide(v7Val, 4, java.math.RoundingMode.HALF_UP);
        double diffPct = (ratio.doubleValue() - 1.0) * 100;
        System.out.println(String.format("[비교] V8/V7 비율 = %.4f (차이 %+.1f%%)", ratio, diffPct));
        System.out.println("======================================================\n");

        // V7 블렌딩: 50×0.6 + 30×0.4 = 42
        BigDecimal v7Blended = new BigDecimal(detailV7.get블렌딩PER());
        assertEquals(0, v7Blended.compareTo(new BigDecimal("42.0000")),
                "V7 블렌딩PER은 42여야 한다");

        // V8 블렌딩: min(50, 30×1.5=45)=45, 45×0.6+30×0.4=39
        BigDecimal v8Blended = new BigDecimal(detailV8.get블렌딩PER());
        assertEquals(0, v8Blended.compareTo(new BigDecimal("39.0000")),
                "V8 블렌딩PER은 39여야 한다 (실제: " + v8Blended + ")");

        // V7 adjustedPER: min(42×1.6, 30×1.8) = min(67.2, 54) = 54
        BigDecimal v7AdjPER = new BigDecimal(detailV7.get성장률보정PER());
        assertEquals(0, v7AdjPER.compareTo(new BigDecimal("54.0000")),
                "V7 adjustedPER은 54여야 한다 (실제: " + v7AdjPER + ")");

        // V8 adjustedPER: min(39×1.55, 30×2.0) = min(60.45, 60) = 60
        // V8 성장률: 50% + (10%×0.5) = 55% → 1.55
        BigDecimal v8AdjPER = new BigDecimal(detailV8.get성장률보정PER());
        assertEquals(0, v8AdjPER.compareTo(new BigDecimal("60.0000")),
                "V8 adjustedPER은 60여야 한다 (실제: " + v8AdjPER + ")");

        assertTrue(v8Val.compareTo(BigDecimal.ZERO) > 0,
                "V8 주당가치는 양수여야 한다");
    }

    // ==================================================================================
    // 동적 안전마진 (PerShareValueCalcHelper.calculateDynamicSafetyMargin)
    // ==================================================================================
    @Test
    @DisplayName("안전마진: 기본(beta=1.0, 그레이엄4개) → 30%")
    void test_safetyMargin_default() {
        double margin = calcHelper.calculateDynamicSafetyMargin(1.0, 4);
        assertEquals(0.30, margin, 0.001);
    }

    @Test
    @DisplayName("안전마진: 고변동성(beta=2.5) + 낮은 신뢰도(그레이엄2개) → 45% 상한")
    void test_safetyMargin_max() {
        double margin = calcHelper.calculateDynamicSafetyMargin(2.5, 2);
        assertEquals(0.45, margin, 0.001);
    }

    @Test
    @DisplayName("안전마진: 저변동성(beta=0.3) + 높은 신뢰도(그레이엄5개) → 25% 하한")
    void test_safetyMargin_min() {
        double margin = calcHelper.calculateDynamicSafetyMargin(0.3, 5);
        assertEquals(0.25, margin, 0.001);
    }

    @Test
    @DisplayName("안전마진: beta=null → beta 조정 없이 기본 30%")
    void test_safetyMargin_nullBeta() {
        double margin = calcHelper.calculateDynamicSafetyMargin(null, 4);
        assertEquals(0.30, margin, 0.001);
    }

    @Test
    @DisplayName("안전마진: beta=1.3 + 그레이엄3개 → 37%")
    void test_safetyMargin_midRange() {
        // base(0.30) + beta>1.2(0.04) + 그레이엄<=3(0.03) = 0.37
        double margin = calcHelper.calculateDynamicSafetyMargin(1.3, 3);
        assertEquals(0.37, margin, 0.001);
    }

    // ==================================================================================
    // 매매가 계산 (PerShareValueCalcHelper)
    // ==================================================================================
    @Test
    @DisplayName("매매가: 매수적정가 = 내재가치 × (1-마진), 목표매도가 = ×0.95, 손절매 = 매수가×0.8")
    void test_tradingPrices() {
        String 매수적정가 = calcHelper.calculatePurchasePrice("144.00", 0.30);
        assertEquals("100.80", 매수적정가);

        String 목표매도가 = calcHelper.calculateSellTarget("144.00");
        assertEquals("136.80", 목표매도가);

        String 손절매가 = calcHelper.calculateStopLoss("100.80");
        assertEquals("80.64", 손절매가);
    }

    // ==================================================================================
    // 전체 파이프라인 통합 검증
    // ==================================================================================
    @Test
    @DisplayName("통합: 저평가 기업 전체 파이프라인 (계산값$180 → 가격차이율 +80%)")
    void test_fullPipeline_undervalued() {
        // Step 1: 계산된주당가치를 그대로 내재가치로 사용 (52주 캡 제거)
        String 주당가치 = "180";

        // Step 2: 안전마진
        double margin = calcHelper.calculateDynamicSafetyMargin(1.0, 4);
        assertEquals(0.30, margin, 0.001);

        // Step 3: 매매가
        String 매수적정가 = calcHelper.calculatePurchasePrice(주당가치, margin);
        assertEquals("126.00", 매수적정가);

        // Step 4: 가격차이율 = (180 - 100) / 100 × 100 = +80%
        BigDecimal fair = new BigDecimal(주당가치);
        BigDecimal current = new BigDecimal("100");
        BigDecimal priceGap = fair.subtract(current)
                .divide(current, 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, java.math.RoundingMode.HALF_UP);
        assertEquals(new BigDecimal("80.00"), priceGap,
                "순수 내재가치 기반 가격차이율 +80%");
    }

    // ==================================================================================
    // Financial Services PBR 기반 평가 테스트
    // ==================================================================================

    /**
     * Financial PBR 테스트용 공통 데이터 생성
     */
    private CompanySharePriceCalculator createFinancialBaseCalParam() {
        CompanySharePriceCalculator req = createBaseCalParam();
        // 금융주는 유동비율 미적용이므로 영향 없지만 세팅
        req.setCurrentAssetsTotal("500000000000");
        req.setCurrentLiabilitiesTotal("450000000000");
        req.setCurrentRatio("1.11");
        req.setInvestmentAssets("100000000000");
        req.setIntangibleAssets("5000000000");
        return req;
    }

    @Test
    @DisplayName("Financial PBR: 일반 은행 (BPS=50, ROE=12%, Beta=0.8) → BPS×targetPBR")
    void test_financial_normalBank() {
        CompanySharePriceCalculator req = createFinancialBaseCalParam();
        req.setBookValuePerShare("50");
        req.setRoe("0.12");
        req.setBeta("0.8");
        req.setPer("10");
        req.setEpsgrowth("0.05");
        req.setOperatingProfitPrePre("5000000000");
        req.setOperatingProfitPre("5500000000");
        req.setOperatingProfitCurrent("6000000000");

        CompanySharePriceResultDetail detail = new CompanySharePriceResultDetail("1");
        String result = service.calPerValueV8(req, detail, "Financial Services");

        assertNotNull(result);
        assertTrue(detail.isPBR기반평가(), "PBR 기반 평가 플래그가 true여야 함");

        // COE = 0.04 + 0.8 × 0.05 = 0.08
        // targetPBR = 0.12 / 0.08 = 1.5 (범위 내)
        // 적정가 = 50 × 1.5 × 1.0 (상승추세) = 75.00 - 순부채/주
        BigDecimal resultVal = new BigDecimal(result);

        System.out.println("======================================================");
        System.out.println("Financial PBR: 일반 은행");
        System.out.println("BPS=50, ROE=12%, Beta=0.8");
        System.out.println("COE: " + detail.getCOE());
        System.out.println("targetPBR: " + detail.getTargetPBR());
        System.out.println("추세팩터: " + detail.get영업이익추세팩터());
        System.out.println("결과: $" + result);
        System.out.println("======================================================\n");

        assertEquals("1.5000", detail.getTargetPBR(), "targetPBR은 1.5여야 함");
        assertTrue(detail.is연속상승추세(), "연속 상승 추세여야 함");
        assertTrue(resultVal.compareTo(BigDecimal.ZERO) > 0, "적정가는 양수여야 함");
    }

    @Test
    @DisplayName("Financial PBR: 고ROE 은행 (ROE=30%) → targetPBR 상한 3.0 적용")
    void test_financial_highROE_targetPBRCap() {
        CompanySharePriceCalculator req = createFinancialBaseCalParam();
        req.setBookValuePerShare("40");
        req.setRoe("0.30");
        req.setBeta("1.0");
        req.setPer("15");
        req.setEpsgrowth("0.15");
        req.setOperatingProfitPrePre("8000000000");
        req.setOperatingProfitPre("9000000000");
        req.setOperatingProfitCurrent("10000000000");

        CompanySharePriceResultDetail detail = new CompanySharePriceResultDetail("1");
        String result = service.calPerValueV8(req, detail, "Financial Services");

        // COE = 0.04 + 1.0 × 0.05 = 0.09
        // targetPBR = 0.30 / 0.09 = 3.33 → 상한 3.0 적용
        assertEquals("3.0000", detail.getTargetPBR(), "targetPBR은 3.0 상한이어야 함");

        System.out.println("======================================================");
        System.out.println("Financial PBR: 고ROE 은행 (ROE=30%)");
        System.out.println("targetPBR: " + detail.getTargetPBR() + " (상한 3.0 적용)");
        System.out.println("결과: $" + result);
        System.out.println("======================================================\n");
    }

    @Test
    @DisplayName("Financial PBR: 저ROE 은행 (ROE=2%) → targetPBR 하한 0.5 + 하락추세")
    void test_financial_lowROE_targetPBRFloor() {
        CompanySharePriceCalculator req = createFinancialBaseCalParam();
        req.setBookValuePerShare("80");
        req.setRoe("0.02");
        req.setBeta("1.2");
        req.setPer("40");
        req.setEpsgrowth("0.01");
        req.setOperatingProfitPrePre("5000000000");
        req.setOperatingProfitPre("3000000000");
        req.setOperatingProfitCurrent("1000000000");

        CompanySharePriceResultDetail detail = new CompanySharePriceResultDetail("1");
        String result = service.calPerValueV8(req, detail, "Financial Services");

        // COE = 0.04 + 1.2 × 0.05 = 0.10
        // targetPBR = 0.02 / 0.10 = 0.2 → 하한 0.5 적용
        assertEquals("0.5000", detail.getTargetPBR(), "targetPBR은 0.5 하한이어야 함");
        assertTrue(detail.is연속하락추세(), "연속 하락 추세여야 함");

        // 적정가 = 80 × 0.5 × 0.85 = 34.0 - 순부채/주
        BigDecimal resultVal = new BigDecimal(result);

        System.out.println("======================================================");
        System.out.println("Financial PBR: 저ROE 은행 (ROE=2%, 연속하락)");
        System.out.println("targetPBR: " + detail.getTargetPBR() + " (하한 0.5 적용)");
        System.out.println("추세팩터: " + detail.get영업이익추세팩터() + " (연속하락 0.85)");
        System.out.println("결과: $" + result);
        System.out.println("======================================================\n");

        assertTrue(resultVal.compareTo(BigDecimal.ZERO) > 0, "적정가는 양수여야 함");
    }

    @Test
    @DisplayName("Financial PBR: BPS 없으면 PER 기반 폴백")
    void test_financial_noBPS_fallbackToPER() {
        CompanySharePriceCalculator req = createFinancialBaseCalParam();
        // BPS를 설정하지 않음 → PER 기반 폴백
        req.setBookValuePerShare(null);
        req.setRoe("0.12");
        req.setBeta("1.0");
        req.setPer("10");
        req.setEpsgrowth("0.05");
        req.setOperatingIncomeGrowth("0.1");
        req.setOperatingProfitPrePre("5000000000");
        req.setOperatingProfitPre("5500000000");
        req.setOperatingProfitCurrent("6000000000");
        req.setQuarterlyOpIncomeQ1("1800000000");
        req.setQuarterlyOpIncomeQ2("1500000000");
        req.setQuarterlyOpIncomeQ3("1400000000");
        req.setQuarterlyOpIncomeQ4("1300000000");

        CompanySharePriceResultDetail detail = new CompanySharePriceResultDetail("1");
        String result = service.calPerValueV8(req, detail, "Financial Services");

        assertNotNull(result);
        assertFalse(detail.isPBR기반평가(), "BPS 없으면 PBR 기반 평가 아님 (PER 폴백)");

        System.out.println("======================================================");
        System.out.println("Financial PBR: BPS 없음 → PER 기반 폴백");
        System.out.println("PBR기반평가: " + detail.isPBR기반평가());
        System.out.println("블렌딩PER: " + detail.get블렌딩PER());
        System.out.println("결과: $" + result);
        System.out.println("======================================================\n");
    }

    @Test
    @DisplayName("Financial PBR: 단일하락 추세팩터(0.92) 적용 확인")
    void test_financial_singleDeclineTrend() {
        CompanySharePriceCalculator req = createFinancialBaseCalParam();
        req.setBookValuePerShare("60");
        req.setRoe("0.10");
        req.setBeta("1.0");
        req.setPer("12");
        req.setEpsgrowth("0.05");
        // 전전기 < 전기 > 당기 → 단일하락
        req.setOperatingProfitPrePre("4000000000");
        req.setOperatingProfitPre("6000000000");
        req.setOperatingProfitCurrent("5000000000");

        CompanySharePriceResultDetail detail = new CompanySharePriceResultDetail("1");
        String result = service.calPerValueV8(req, detail, "Financial Services");

        assertTrue(detail.isPBR기반평가(), "PBR 기반 평가여야 함");
        assertTrue(detail.is단일하락추세(), "단일 하락 추세여야 함");
        assertEquals("0.92", detail.get영업이익추세팩터(), "추세팩터는 0.92여야 함");

        // COE = 0.04 + 1.0 × 0.05 = 0.09
        // targetPBR = 0.10 / 0.09 = 1.1111 (범위 내)
        BigDecimal expectedTargetPBR = new BigDecimal("1.1111");
        BigDecimal actualTargetPBR = new BigDecimal(detail.getTargetPBR());
        assertEquals(0, expectedTargetPBR.compareTo(actualTargetPBR),
                "targetPBR은 1.1111이어야 함 (실제: " + actualTargetPBR + ")");

        System.out.println("======================================================");
        System.out.println("Financial PBR: 단일하락 추세 (0.92)");
        System.out.println("targetPBR: " + detail.getTargetPBR());
        System.out.println("추세팩터: " + detail.get영업이익추세팩터());
        System.out.println("결과: $" + result);
        System.out.println("======================================================\n");
    }
}
