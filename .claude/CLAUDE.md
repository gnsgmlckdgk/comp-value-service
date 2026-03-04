# CompValue Backend - 주당가치 계산 고도화 이력

## 현재 운영 버전: V8

`EvaluationConst.CAL_VALUE_VERSION = "v8"`

---

## 버전별 고도화 이력

### V1 - 기본 적정가 계산
- **공식**: `영업이익평균 × 고정PER(10) + 재산가치 - 부채` ÷ 발행주식수
- 재산가치 = 유동자산 - (유동부채 × 유동비율) + 투자자산
- 고정 PER 10 사용, 성장률 미반영
- 파일: `PerShareValueCalculationService.calPerValue()`

### V2 - 성장률 반영 PER 도입
- **adjustedPER** = PER × (1 + 영업이익성장률)
- PEG 비율 도입: PER / EPS성장률
- R&D 3년 평균 계산 추가
- 순부채(총부채 - 현금) 차감 도입
- 무형자산 가중치 0.3 (고정)
- **추가 필드**: PER, PEG, EPS성장률, 영업이익성장률, R&D(3년), 순부채

### V3 - 적자/고성장 기업 PSR 경로
- PER ≤ 0 또는 > 100이면 수익가치 계산불가 판정
- **PSR 경로**: 매출액 × PSR × 매출성장률보정계수 (매출성장률 > 20% 시)
- 매출성장률보정계수: 20~30%→0.3, 30~50%→0.5, 50~80%→0.7, 80%+→0.9
- 흑자전환기업 플래그 추가
- adjustedPER 상한: 실제PER × 2.5
- **추가 필드**: 수익가치계산불가, 적자기업, 매출기반평가, 흑자전환기업, 매출액, PSR

### V4 - 섹터별 파라미터 도입
- `SectorParameterFactory` + `SectorCalculationParameters` 신설
- 11개 섹터별 차등 파라미터: basePER, 무형자산가중치, maxPSR, 성장률상한, R&D가중치
- 섹터별 유동비율 적용 여부 (금융업 미적용)
- **해결**: 테크/바이오 vs 유틸리티 등 섹터 특성 무시 문제

### V5 - (예약, V4와 동일)
- `calPerValueV5()` → 내부적으로 V4 호출

### V6 - PER 블렌딩 + 추세팩터
- **PER 블렌딩**: `실제PER×0.6 + 섹터PER×0.4`
- **가중평균 영업이익**: 전전기×1 + 전기×2 + 당기×3 (÷6)
- **연간 추세팩터**: 연속하락 0.8, 단일하락 0.9
- **분기 추세팩터**: Q1<Q2이고 최근반기<이전반기 → 0.7, Q1<Q2만 → 0.85
- adjustedPER 상한: 섹터PER × 2.5
- **추가 필드**: 블렌딩PER, 분기영업이익(Q1~Q4), 분기추세팩터, 영업이익추세팩터

### V7 - 흑자전환 보수화 + 그레이엄 스크리닝
- 흑자전환기업 특별처리: PER 블렌딩 시 섹터 평균 쪽으로 보수적 블렌딩
- **분기 적자전환 감지**: Q1 < 0 && Q2 > 0 → 추세팩터 × 0.5
- adjustedPER 상한 강화: 섹터PER × 1.8 (V6의 2.5에서 축소)
- **그레이엄 스크리닝 5항목**: PER통과, PBR통과, PER×PBR통과, 유동비율통과, 연속흑자통과
- 그레이엄 등급: 강력매수(5/5), 매수(4/5), 관망(3/5), 위험(0~2/5)
- **추가 필드**: 그레이엄 관련 7개, PBR, 급락종목할인

### V8 - 보수적 PER 블렌딩 + PBR 금융주 + 모멘텀 게이트 + 6단계 점수체계
**핵심 변경:**

1. **PER 블렌딩 보수화**
   - `cappedPER = min(실제PER, 섹터PER×1.5)` → 이후 블렌딩
   - adjustedPER 상한: 섹터PER × 2.0 (V7의 1.8에서 조정)

2. **고성장률 지속가능성 할인**
   - 성장률 50% 초과분은 50%만 인정
   - 예: 80% → 50% + (30%×0.5) = 65%

3. ~~**52주 최고가 캡**~~ (제거됨 - 순수 내재가치 기반으로 전환)
   - 내재가치는 펀더멘털로만 산출, 시장가격은 매수/매도 판단 시점에서만 비교

4. **동적 안전마진** (25~45% 범위)
   - 기본 30% + Beta 조정 + 그레이엄 통과수 조정

5. **매매가 산출**
   - 매수적정가 = 적정가 × (1 - 안전마진)
   - 목표매도가 = 적정가 × 0.95
   - 손절매가 = 매수적정가 × 0.8

6. **모멘텀/기술적 분석** (게이트 제거됨)
   - SMA50/SMA200, RSI, 거래량 분석 → Step6 점수(0~18점)로 자연 반영
   - 데스크로스 등 약세 신호는 Step6 저점수로 충분히 감점 (총점 상한 강제 적용 없음)

7. **6단계 점수체계** (총 100점)
   - Step1 위험신호(12점), Step2 신뢰도(18점), Step3 밸류에이션(20점)
   - Step4 영업이익추세(15점), Step5 투자적합성(17점), Step6 모멘텀(18점)

8. **PBR 기반 금융주 평가** (Financial Services 전용)
   - COE = 무위험수익률(4%) + Beta × 시장위험프리미엄(5%)
   - targetPBR = ROE / COE (범위 0.5~3.0)
   - 적정가 = BPS × targetPBR × 추세팩터 - 순부채/주
   - BPS 없으면 기존 PER 기반으로 자동 폴백
   - Step3/Step5 점수체계도 PBR-relative로 분기

9. **과대평가 의심 할인**
   - Step5에서 적정가 대비 고평가 비율에 따라 PEG/그레이엄 점수 단계적 감점

10. **극단값 방지 안전장치**
    - PSR 경로 STEP01 상한: 시가총액 × 3 초과 방지 (적자 고매출 기업의 적정가 폭주 방지)
    - 추세팩터 복합 하한 0.5: 연간×분기 추세팩터 곱이 0.5 미만이 되지 않도록 보호
    - Step3 성장률: 적정가에서 이미 50%초과분 할인 적용하므로 점수에서 이중 감점 없음
    - Step4 당기급증: 별도 의심 분기 없이 일반 추세 로직으로 평가

- **추가 필드**: SMA50/200, RSI, 거래량비율, 모멘텀점수, 안전마진율, 과대평가의심할인, PBR기반평가, BPS, ROE, COE, targetPBR

---

## 계산 구조 (전 버전 공통 골격)

```
STEP01 = 영업이익가중평균 × adjustedPER × 추세팩터들
STEP02 = 유동자산 - (유동부채 × K) + 투자자산       ← K: 유동비율 기반 차감계수
STEP03 = 무형자산 × 섹터가중치
STEP05 = 순부채 (총부채 - 현금)

기업가치 = STEP01 + STEP02 + STEP03 - STEP05
주당가치 = 기업가치 ÷ 발행주식수
```

V8 Financial Services (PBR 경로):
```
COE = riskFreeRate + Beta × marketRiskPremium
targetPBR = ROE / COE (0.5 ≤ targetPBR ≤ 3.0)
주당가치 = BPS × targetPBR × 추세팩터 - 순부채/주
```

V8 후처리:
```
→ 동적 안전마진 → 매매가 산출 (순수 내재가치 기반)
```

---

## 주요 파일

| 파일 | 역할 |
|------|------|
| `PerShareValueCalculationService.java` | V1 + V8 계산 로직 |
| `PerShareValueCalcLegacyService.java` | V2~V7 레거시 계산 로직 |
| `PerShareValueCalcHelper.java` | 공통 헬퍼 (PEG, 가중평균, 안전마진, 매매가) |
| `US_StockCalFromFpmService.java` | FMP 데이터 수집 + 계산 오케스트레이션 |
| `StockEvaluationService.java` | 6단계 점수 평가 |
| `SectorParameterFactory.java` | 섹터별 파라미터 팩토리 |
| `SectorCalculationParameters.java` | 섹터 파라미터 DTO |
| `CompanySharePriceCalculator.java` | 계산 입력 DTO |
| `CompanySharePriceResultDetail.java` | 계산 결과 상세 DTO |
| `EvaluationConst.java` | 평가 상수 (가중치, 기준값) |

---

## 변경 시 주의사항

- 새 버전 추가 시 반드시 독립 메서드 세트 생성 (버전 간 메서드 호출 금지)
- FMP API 종목당 10회 이내 유지 (rate limit: 분당 300건)
- `CompanySharePriceResultDetail`에 필드 추가 시 기본값 설정 필수 (Redis 역직렬화 호환)
- 섹터 파라미터 변경 시 전체 테스트 regression 확인
