package com.finance.dart.api.common.constants;

/**
 * 평가 시스템 상수
 */
public class EvaluationConst {

    /**
     * Redis 키 버전
     */
    public static final String CAL_VALUE_VERSION = "v8";

    /**
     * 각 Step별 가중치 (총 100점)
     * Step 1: 위험 신호 확인 - 치명적 결함 체크 (감점 방식)
     * Step 2: 신뢰도 확인 - 재무 건전성 평가
     * Step 3: 밸류에이션 평가 - PEG, 과대평가 위험
     * Step 4: 영업이익 추세 - 성장 지속가능성
     * Step 5: 투자 적합성 - 매수적정가(세분화), 그레이엄(강화)
     * Step 6: 모멘텀/기술적 분석 - 이동평균, RSI, 거래량 추세
     */
    public static final int STEP1_WEIGHT = 12;  // 위험 신호 확인 (치명적 결함 필터)
    public static final int STEP2_WEIGHT = 18;  // 신뢰도 확인
    public static final int STEP3_WEIGHT = 20;  // 밸류에이션 평가
    public static final int STEP4_WEIGHT = 15;  // 영업이익 추세 확인
    public static final int STEP5_WEIGHT = 17;  // 투자 적합성
    public static final int STEP6_WEIGHT = 18;  // 모멘텀/기술적 분석

    /**
     * Step 5 서브점수 (이중 계산 해소: PEG/PSR/PBR 이진 판단 제거, Step3과 중복 방지)
     */
    public static final int STEP5_PURCHASE_PRICE = 9;  // 매수적정가 vs 현재가 (세분화)
    public static final int STEP5_GRAHAM = 8;           // 그레이엄 기준 (강화)

    /**
     * Step 설명
     */
    public static final String STEP1_DESC = "위험 신호 확인: 수익가치계산불가, 적자기업, 매출기반평가 등 치명적 결함을 체크합니다. 문제 발견 시 대폭 감점됩니다.";
    public static final String STEP2_DESC = "신뢰도 확인: PER, 순부채, 영업이익 안정성을 종합적으로 평가하여 기업의 재무 건전성을 판단합니다.";
    public static final String STEP3_DESC = "밸류에이션 평가: PEG, 가격 차이, 성장률 등을 분석하여 현재 주가가 적정한지 또는 저평가/고평가 되었는지 판단합니다.";
    public static final String STEP4_DESC = "영업이익 추세: 최근 3년간 영업이익 추세를 분석하여 성장의 지속가능성을 평가합니다.";
    public static final String STEP5_DESC = "투자 적합성: 매수적정가 대비 현재가 세분화 평가와 그레이엄 스크리닝 결과를 종합하여 최종 투자 적합성을 판단합니다.";
    public static final String STEP6_DESC = "모멘텀/기술적 분석: 이동평균선(SMA50/200), RSI, 거래량 추세를 분석하여 매수 타이밍의 적절성을 판단합니다.";

    /**
     * 음수 적정가 게이트 상한 점수 (적정가 ≤ 0일 때)
     */
    public static final double NEGATIVE_FAIR_VALUE_MAX_SCORE = 50.0;

    /**
     * PER 기준값
     */
    public static final double PER_MIN_NORMAL = 5.0;
    public static final double PER_MAX_NORMAL = 30.0;
    public static final double PER_HIGH_RISK = 40.0;

    /**
     * PEG 기준값
     */
    public static final double PEG_UNDERVALUED = 1.0;
    public static final double PEG_FAIR = 1.5;
    public static final double PEG_OVERVALUED = 2.0;
    public static final double PEG_HIGH_RISK = 2.5;

    /**
     * 성장률 기준값
     */
    public static final double GROWTH_SUSTAINABLE = 0.5;    // 50%
    public static final double GROWTH_HIGH_RISK = 0.8;      // 80%
    public static final double GROWTH_VERY_HIGH_RISK = 1.0; // 100%

}
