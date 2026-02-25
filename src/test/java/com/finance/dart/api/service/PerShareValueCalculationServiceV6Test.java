package com.finance.dart.api.service;

import com.finance.dart.api.common.context.RequestContext;
import com.finance.dart.api.common.dto.CompanySharePriceCalculator;
import com.finance.dart.api.common.dto.CompanySharePriceResultDetail;
import com.finance.dart.api.common.service.PerShareValueCalcHelper;
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
class PerShareValueCalculationServiceV6Test {

    @Mock
    private RequestContext requestContext;

    private PerShareValueCalcHelper calcHelper;

    private PerShareValueCalcLegacyService legacyService;

    @BeforeEach
    void setUp() {
        calcHelper = new PerShareValueCalcHelper(requestContext);
        legacyService = new PerShareValueCalcLegacyService(calcHelper);
    }

    /**
     * 공통 테스트 데이터 생성 헬퍼
     */
    private CompanySharePriceCalculator createBaseCalParam() {
        CompanySharePriceCalculator req = new CompanySharePriceCalculator();
        req.setCurrentAssetsTotal("50000000000");       // 500억 유동자산
        req.setCurrentLiabilitiesTotal("20000000000");  // 200억 유동부채
        req.setCurrentRatio("2.5");                      // 유동비율
        req.setInvestmentAssets("5000000000");            // 50억 투자자산
        req.setIntangibleAssets("3000000000");            // 30억 무형자산
        req.setTotalDebt("15000000000");                 // 150억 총부채
        req.setCashAndCashEquivalents("10000000000");    // 100억 현금
        req.setIssuedShares("1000000000");               // 10억주
        req.setEpsgrowth("0.25");                        // EPS성장률 25%
        req.setRndCurrent("1000000000");
        req.setRndPre("900000000");
        req.setRndPrePre("800000000");
        req.setRevenue("100000000000");                  // 매출 1000억
        req.setRevenueGrowth("0.15");                    // 매출성장률 15%
        req.setPsr("5");
        return req;
    }

    @Test
    @DisplayName("시나리오1: 정상 수익 기업 (Technology, PER=25, 성장률 30%) - 블렌딩PER 검증")
    void test_scenario1_normalProfitCompany_blendedPER() {
        // given
        CompanySharePriceCalculator req = createBaseCalParam();
        req.setPer("25");
        req.setOperatingIncomeGrowth("0.3");
        req.setOperatingProfitPrePre("8000000000");  // 80억 (상승추세)
        req.setOperatingProfitPre("9000000000");     // 90억
        req.setOperatingProfitCurrent("10000000000"); // 100억

        // 분기도 안정적 (상승)
        req.setQuarterlyOpIncomeQ1("3000000000");
        req.setQuarterlyOpIncomeQ2("2800000000");
        req.setQuarterlyOpIncomeQ3("2500000000");
        req.setQuarterlyOpIncomeQ4("2200000000");

        CompanySharePriceResultDetail detail = new CompanySharePriceResultDetail("1");
        String sector = "Technology"; // basePER=30

        // when
        String result = legacyService.calPerValueV6(req, detail, sector);

        // then
        assertNotNull(result);
        BigDecimal resultVal = new BigDecimal(result);
        assertTrue(resultVal.signum() > 0, "주당가치는 양수여야 한다");

        // 블렌딩PER 검증: 25*0.6 + 30*0.4 = 27
        assertEquals("27.0000", detail.get블렌딩PER());
        assertEquals("25", detail.get실제PER());
        assertEquals("30", detail.get섹터PER());

        // 연속 상승 추세
        assertTrue(detail.is연속상승추세());
        assertFalse(detail.is연속하락추세());
        assertEquals("1", detail.get영업이익추세팩터());

        // 분기 실적 안정 (Q1 > Q2 이므로 악화 아님)
        assertFalse(detail.is분기실적악화());
        assertEquals("1", detail.get분기추세팩터());
    }

    @Test
    @DisplayName("시나리오2: 연속 하락 기업 (Industrials) - 연간 0.8 × 분기 0.7 = 56% 감산")
    void test_scenario2_consecutiveDecline_annualAndQuarterly() {
        // given
        CompanySharePriceCalculator req = createBaseCalParam();
        req.setPer("15");
        req.setOperatingIncomeGrowth("0.1");
        // 연속 하락: 당기(6B) < 전기(8B) < 전전기(10B)
        req.setOperatingProfitPrePre("10000000000");
        req.setOperatingProfitPre("8000000000");
        req.setOperatingProfitCurrent("6000000000");

        // 연속 분기 악화: Q1 < Q2 AND (Q1+Q2) < (Q3+Q4)
        req.setQuarterlyOpIncomeQ1("1000000000");  // Q1=10억
        req.setQuarterlyOpIncomeQ2("1500000000");  // Q2=15억
        req.setQuarterlyOpIncomeQ3("2000000000");  // Q3=20억
        req.setQuarterlyOpIncomeQ4("2500000000");  // Q4=25억

        CompanySharePriceResultDetail detail = new CompanySharePriceResultDetail("1");
        String sector = "Industrials"; // basePER=18

        // when
        String result = legacyService.calPerValueV6(req, detail, sector);

        // then
        assertNotNull(result);

        // 연간 추세: 연속 하락 → 0.8
        assertTrue(detail.is연속하락추세());
        assertEquals("0.8", detail.get영업이익추세팩터());

        // 분기 추세: 연속 분기 악화 → 0.7
        assertTrue(detail.is분기실적악화());
        assertEquals("0.7", detail.get분기추세팩터());
    }

    @Test
    @DisplayName("시나리오3: 시장 경계 종목 (PER=8, 섹터PER=30) - 블렌딩으로 보수적 평가")
    void test_scenario3_lowPER_blendingMoreConservative() {
        // given
        CompanySharePriceCalculator req = createBaseCalParam();
        req.setPer("8");
        req.setOperatingIncomeGrowth("0.1");
        req.setOperatingProfitPrePre("5000000000");
        req.setOperatingProfitPre("5000000000");
        req.setOperatingProfitCurrent("5000000000");

        // 분기 데이터 없음 → 할인 없음
        req.setQuarterlyOpIncomeQ1(null);
        req.setQuarterlyOpIncomeQ2(null);
        req.setQuarterlyOpIncomeQ3(null);
        req.setQuarterlyOpIncomeQ4(null);

        CompanySharePriceResultDetail detail = new CompanySharePriceResultDetail("1");
        String sector = "Technology"; // basePER=30

        // when
        String result = legacyService.calPerValueV6(req, detail, sector);

        // then
        assertNotNull(result);

        // 블렌딩PER = 8*0.6 + 30*0.4 = 4.8 + 12 = 16.8
        assertEquals("16.8000", detail.get블렌딩PER());

        // V5라면 섹터PER=30을 사용했을 것 → V6은 16.8로 훨씬 보수적
        BigDecimal blended = new BigDecimal(detail.get블렌딩PER());
        assertTrue(blended.compareTo(new BigDecimal("30")) < 0,
                "블렌딩PER(16.8)은 섹터PER(30)보다 낮아야 한다");

        // 분기 데이터 없으면 분기추세팩터는 1 (할인 없음)
        assertEquals("1", detail.get분기추세팩터());
    }

    @Test
    @DisplayName("시나리오4: 적자 고성장 기업 (PER 음수, 매출성장 50%) - PSR 경로 + 분기추세 할인")
    void test_scenario4_lossCompany_highGrowth_PSR() {
        // given
        CompanySharePriceCalculator req = createBaseCalParam();
        req.setPer("-15");
        req.setOperatingIncomeGrowth("-0.5");
        req.setOperatingProfitPrePre("-2000000000");
        req.setOperatingProfitPre("-3000000000");
        req.setOperatingProfitCurrent("-1000000000");
        req.setRevenueGrowth("0.5");  // 매출성장률 50% → PSR 경로
        req.setRevenue("50000000000");
        req.setPsr("8");

        // 단일 분기 악화: Q1 < Q2
        req.setQuarterlyOpIncomeQ1("-500000000");
        req.setQuarterlyOpIncomeQ2("-300000000");
        req.setQuarterlyOpIncomeQ3("-200000000");
        req.setQuarterlyOpIncomeQ4("-100000000");

        CompanySharePriceResultDetail detail = new CompanySharePriceResultDetail("1");
        String sector = "Technology";

        // when
        String result = legacyService.calPerValueV6(req, detail, sector);

        // then
        assertNotNull(result);
        assertTrue(detail.is적자기업());
        assertTrue(detail.is매출기반평가());
        assertTrue(detail.is분기실적악화());

        // PSR 경로이므로 블렌딩PER은 N/A
        assertTrue(detail.get블렌딩PER().contains("PSR경로"));
    }

    @Test
    @DisplayName("시나리오5: V5 vs V6 비교 - 동일 입력에서 V6가 더 보수적 (낮은 값)")
    void test_scenario5_v6MoreConservativeThanV5() {
        // given: 하락 추세 기업 (V6 할인이 가장 크게 작용하는 케이스)
        CompanySharePriceCalculator req = createBaseCalParam();
        req.setPer("20");
        req.setOperatingIncomeGrowth("0.15");
        // 단일 하락: 당기 < 전기
        req.setOperatingProfitPrePre("7000000000");
        req.setOperatingProfitPre("9000000000");
        req.setOperatingProfitCurrent("8000000000");

        // 분기 악화
        req.setQuarterlyOpIncomeQ1("1500000000");
        req.setQuarterlyOpIncomeQ2("2000000000");
        req.setQuarterlyOpIncomeQ3("2200000000");
        req.setQuarterlyOpIncomeQ4("2300000000");

        String sector = "Technology"; // basePER=30

        // V5 계산 (legacy)
        CompanySharePriceResultDetail detailV5 = new CompanySharePriceResultDetail("1");
        String resultV5 = legacyService.calPerValueV5(req, detailV5, sector);

        // V6 계산 (legacy)
        CompanySharePriceResultDetail detailV6 = new CompanySharePriceResultDetail("1");
        String resultV6 = legacyService.calPerValueV6(req, detailV6, sector);

        // then
        assertNotNull(resultV5);
        assertNotNull(resultV6);

        BigDecimal v5Val = new BigDecimal(resultV5);
        BigDecimal v6Val = new BigDecimal(resultV6);

        assertTrue(v6Val.compareTo(v5Val) < 0,
                String.format("V6(%s)는 V5(%s)보다 보수적(낮은 값)이어야 한다", resultV6, resultV5));

        // V6 추세팩터 확인
        assertTrue(detailV6.is단일하락추세());   // 당기 < 전기
        assertEquals("0.9", detailV6.get영업이익추세팩터());
        assertTrue(detailV6.is분기실적악화());     // Q1 < Q2
    }
}
