package com.finance.dart.api.common.service;

import com.finance.dart.api.common.constants.RequestContextConst;
import com.finance.dart.api.common.context.RequestContext;
import com.finance.dart.common.util.CalUtil;
import com.finance.dart.common.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * PerShareValueCalculationService 공유 헬퍼
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class PerShareValueCalcHelper {

    private final RequestContext requestContext;

    /**
     * 영업이익 가중평균을 계산한다. (V7: 1:2:3 가중치)
     *
     * @param profitPrePre 전전기 영업이익
     * @param profitPre    전기 영업이익
     * @param profitCurrent 당기 영업이익
     * @return 영업이익 가중평균 값
     */
    public String calOperatingProfitWeightedAvg(String profitPrePre, String profitPre, String profitCurrent) {
        String w1 = CalUtil.multi(profitPrePre, "1");
        String w2 = CalUtil.multi(profitPre, "2");
        String w3 = CalUtil.multi(profitCurrent, "3");
        String weightedSum = CalUtil.add(CalUtil.add(w1, w2), w3);
        return CalUtil.divide(weightedSum, "6", 2, RoundingMode.HALF_UP);
    }

    /**
     * 영업이익 평균을 계산한다.
     *
     * @param profitPrePre 전전기 영업이익
     * @param profitPre    전기 영업이익
     * @param profitCurrent 당기 영업이익
     * @return 영업이익 평균 값
     */
    public String calOperatingProfitAvg(String profitPrePre, String profitPre, String profitCurrent) {
        // 전전기 + 전기 + 당기를 한 번에 더한 후 평균 계산
        final String sum = CalUtil.add(CalUtil.add(profitPrePre, profitPre), profitCurrent);
        final String avg = CalUtil.divide(sum, "3", 2, RoundingMode.HALF_UP);

        requestContext.setAttribute(RequestContextConst.영업이익_합계, sum);
        requestContext.setAttribute(RequestContextConst.영업이익_평균, avg);

        return avg;
    }

    /**
     * 연구개발비 평균을 계산한다.
     */
    public String calRnDAvg(String rndPrePre, String rndPre, String rndCurrent) {
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
     * - 유동비율 < 1.0    -> 1.0 (전액 차감, 단기 상환 위험 높음)
     * - 1.0 ~ 1.5 미만   -> 0.7
     * - 1.5 ~ 2.0 미만   -> 0.5
     * - 2.0 이상        -> 0.3 (유동성 충분)
     *
     * @param currentRatio 유동비율 (totalCurrentAssets / totalCurrentLiabilities)
     * @return 유동부채 차감 비율 (0.3 ~ 1.0)
     */
    public double getLiabilityFactor(Double currentRatio) {
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
     */
    public String calAdjustedPER(String incomGrowth, String per) {
        if (StringUtil.isStringEmpty(incomGrowth) || StringUtil.isStringEmpty(per)) return null;

        BigDecimal g = new BigDecimal(incomGrowth);

        // 역성장/정체(<=0)면 보정하지 않음: 기본 PER 반환
        if (g.signum() <= 0) return per;

        // 1) lambda-보정: 너무 작은 성장률 안정화
        BigDecimal lambda = new BigDecimal("0.05");     // 최소 기대 성장 5%
        BigDecimal gEff = g.add(lambda);                    // g + lambda

        // (선택) 상한 캡: 일시적 급등 완화
        BigDecimal gMax = new BigDecimal("0.40");       // 최대 40% 사용
        if (gEff.compareTo(gMax) > 0) gEff = gMax;

        // 2) 이론값: PER / (g + lambda)
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
     * PEG = PER / EPS성장률 -- 1 이하이면 성장 대비 저평가, 1 이상이면 고평가 가능
     * </pre>
     */
    public String calPeg(String per, String epsGrowth) {

        try {
            if (StringUtil.isStringEmpty(per) || StringUtil.isStringEmpty(epsGrowth)) return null;

            BigDecimal perBd = new BigDecimal(per);
            BigDecimal gBd   = new BigDecimal(epsGrowth); // abs() 쓰지 않음

            // 역성장/정체 -> PEG는 의미 없음
            if (gBd.signum() <= 0) return "999";

            // 분모는 "퍼센트". 0.x(비율)로 들어오면 x100, 이미 퍼센트(>1)이면 그대로
            BigDecimal growthPctBd = (gBd.compareTo(BigDecimal.ONE) > 0)
                    ? gBd
                    : gBd.multiply(new BigDecimal("100"));

            return perBd.divide(growthPctBd, 4, RoundingMode.HALF_UP).toPlainString();
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * 매출성장률에 따른 보정계수
     */
    public String getRevenueGrowthFactor(BigDecimal revenueGrowth) {
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
