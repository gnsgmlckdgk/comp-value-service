package com.finance.dart.api.service;

import com.finance.dart.api.domestic.dto.StockValueManualReqDTO;
import com.finance.dart.api.domestic.service.CalCompanyStockPerValueManualService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalCompanyStockPerValueManualServiceTest {

    // 의존성이 없는 서비스라 new로 인스턴스 생성 가능
    private final CalCompanyStockPerValueManualService service = new CalCompanyStockPerValueManualService();

    @Test
    void testCalPerValue() {
        // 테스트용 DTO 값 설정 (예시 값)
        StockValueManualReqDTO req = new StockValueManualReqDTO();
        req.setOperatingProfitPrePre("100");
        req.setOperatingProfitPre("150");
        req.setOperatingProfitCurrent("200");
        req.setCurrentAssetsTotal("1000");
        req.setCurrentLiabilitiesTotal("200");
        req.setCurrentRatio("5");
        req.setInvestmentAssets("50");
        req.setFixedLiabilities("100");
        req.setIssuedShares("9999999999");
        req.setUnit("100000000");

        // 계산 메서드 실행
        String result = service.calPerValue(req);

        // 예상 결과 계산 (예시)
        // 결과: 15
        assertEquals("15", result, "한 주당 계산 결과가 예상과 달라요");
    }
}