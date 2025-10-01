package com.finance.dart.api.abroad.service;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.common.dto.CompanySharePriceCalculator;
import com.finance.dart.api.common.dto.CompanySharePriceResult;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 해외기업 주식가치 계산 서비스
 */
@Slf4j
@AllArgsConstructor
@Service
public class US_StockCalFromFpmService {

    private final FmpClientComponent fmpClientComponent;


    /**
     * 주당 가치 계산
     * @param symbol
     * @return
     */
    public CompanySharePriceResult calPerValue(String symbol) {

        CompanySharePriceResult result = new CompanySharePriceResult();             // 결과
        CompanySharePriceCalculator calParam = new CompanySharePriceCalculator();   // 계산 정보

        //@1. 정보 조회

        //@2. 계산

        //@3. 결과조회

        return null;
    }

    /**
     * 계산정보 조회
     * @param symbol
     * @return
     */
    private CompanySharePriceCalculator getCalParamData(String symbol) {

        return null;
    }

}
