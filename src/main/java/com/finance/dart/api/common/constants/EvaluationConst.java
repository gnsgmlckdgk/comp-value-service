package com.finance.dart.api.common.constants;

/**
 * 평가 시스템 상수
 */
public class EvaluationConst {

    /**
     * Redis 키 버전
     */
    public static final String CAL_VALUE_VERSION = "v5";

    /**
     * 각 Step별 가중치 (총 100점)
     * Step 1: 위험 신호 확인 - 치명적 결함 체크 (감점 방식)
     * Step 2: 신뢰도 확인 - 재무 건전성 평가
     * Step 3: 밸류에이션 평가 - PEG, 과대평가 위험 (가장 중요)
     * Step 4: 영업이익 추세 - 성장 지속가능성
     */
    public static final int STEP1_WEIGHT = 20;  // 위험 신호 확인 (치명적 결함 필터)
    public static final int STEP2_WEIGHT = 25;  // 신뢰도 확인
    public static final int STEP3_WEIGHT = 40;  // 밸류에이션 평가 (가장 중요!)
    public static final int STEP4_WEIGHT = 15;  // 영업이익 추세 확인

    /**
     * Step 설명
     */
    public static final String STEP1_DESC = "위험 신호 확인: 수익가치계산불가, 적자기업, 매출기반평가 등 치명적 결함을 체크합니다. 문제 발견 시 대폭 감점됩니다.";
    public static final String STEP2_DESC = "신뢰도 확인: PER, 순부채, 영업이익 안정성 등을 종합적으로 평가하여 기업의 재무 건전성을 판단합니다.";
    public static final String STEP3_DESC = "밸류에이션 평가: PEG, 가격 차이, 성장률 등을 분석하여 현재 주가가 적정한지 또는 저평가/고평가 되었는지 판단합니다. (가장 중요)";
    public static final String STEP4_DESC = "영업이익 추세: 최근 3년간 영업이익 추세를 분석하여 성장의 지속가능성을 평가합니다.";

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
