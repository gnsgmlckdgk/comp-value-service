package com.finance.dart.api.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

import com.finance.dart.api.dto.*;
import com.finance.dart.api.enums.ExchangeCd;
import com.finance.dart.common.service.HttpClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CalCompanyStockPerValueServiceTest {

    @Mock
    private CorpCodeService corpCodeService;

    @Mock
    private FinancialStatementService financialStatementService;

    @Mock
    private NumberOfSharesIssuedService numberOfSharesIssuedService;

    @Mock
    private HttpClientService httpClientService;

    @InjectMocks
    private CalCompanyStockPerValueService service;

    private CorpCodeDTO dummyCorpCodeDTO;
    private FinancialStatementResDTO dummyFsRes;
    private NumberOfSharesIssuedResDTO dummySharesRes;
    private StockPriceDTO dummyStockPriceDTO;

    @BeforeEach
    void setUp() {
        // Dummy CorpCodeDTO
        dummyCorpCodeDTO = new CorpCodeDTO();
        dummyCorpCodeDTO.setCorpCode("12345678");
        dummyCorpCodeDTO.setCorpName("Test Company");
        dummyCorpCodeDTO.setStockCode("000001");

        // Dummy FinancialStatementResDTO (동일 데이터가 전전기, 전기, 당기에 사용된다고 가정)
        dummyFsRes = new FinancialStatementResDTO();
        FinancialStatementDTO currentAssets = new FinancialStatementDTO();
        currentAssets.setAccountId("ifrs-full_CurrentAssets");
        currentAssets.setThstrmAmount("1000");

        FinancialStatementDTO currentLiabilities = new FinancialStatementDTO();
        currentLiabilities.setAccountId("ifrs-full_CurrentLiabilities");
        currentLiabilities.setThstrmAmount("500");

        FinancialStatementDTO noncurrentLiabilities = new FinancialStatementDTO();
        noncurrentLiabilities.setAccountId("ifrs-full_NoncurrentLiabilities");
        noncurrentLiabilities.setThstrmAmount("100");

        FinancialStatementDTO operatingIncome = new FinancialStatementDTO();
        operatingIncome.setAccountId("dart_OperatingIncomeLoss");
        operatingIncome.setThstrmAmount("300");
        operatingIncome.setThstrmAddAmount("0");

        List<FinancialStatementDTO> fsList = new LinkedList<>();
        fsList.add(currentAssets);
        fsList.add(currentLiabilities);
        fsList.add(noncurrentLiabilities);
        fsList.add(operatingIncome);
        dummyFsRes.setList(fsList);

        // Dummy NumberOfSharesIssuedResDTO
        dummySharesRes = new NumberOfSharesIssuedResDTO();
        NumberOfSharesIssuedDTO shareDTO = new NumberOfSharesIssuedDTO();
        shareDTO.setSe("보통주");
        shareDTO.setIstc_totqy("100");
        dummySharesRes.setList(Collections.singletonList(shareDTO));

        // Dummy StockPriceDTO (Yahoo Finance 응답 모킹)
        dummyStockPriceDTO = new StockPriceDTO();
        StockPriceDTO.Meta meta = new StockPriceDTO.Meta();
        meta.setInstrumentType("EQUITY");
        meta.setRegularMarketPrice(20.0);
        meta.setFullExchangeName(ExchangeCd.코스피.getFullExchangeName());
        StockPriceDTO.Result result = new StockPriceDTO.Result();
        result.setMeta(meta);
        StockPriceDTO.Chart chart = new StockPriceDTO.Chart();
        chart.setResult(Collections.singletonList(result));
        dummyStockPriceDTO.setChart(chart);
    }

    @Test
    void testCalPerValue_HappyPath() {
        String year = "2023";
        String corpCode = "12345678";
        String corpName = "Test Company";

        // 모킹: corpCodeService.getCorpCode(true)
        CorpCodeResDTO corpCodeRes = new CorpCodeResDTO();
        corpCodeRes.setList(Collections.singletonList(dummyCorpCodeDTO));
        when(corpCodeService.getCorpCode(true)).thenReturn(corpCodeRes);

        // 모킹: financialStatmentService.getCompanyFinancialStatement(...) – 모든 재무제표 호출에 대해 dummyFsRes 반환
        when(financialStatementService.getCompanyFinancialStatement(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(dummyFsRes);

        // 모킹: numberOfSharesIssuedService.getNumberOfSharesIssued(...) 반환
        when(numberOfSharesIssuedService.getNumberOfSharesIssued(anyString(), anyString(), anyString()))
                .thenReturn(dummySharesRes);

        // 모킹: httpClientService.exchangeSync(...) – 현재 가격 조회용
        when(httpClientService.exchangeSync(anyString(), any(HttpMethod.class), any(HttpEntity.class), any()))
                .thenReturn(ResponseEntity.ok(dummyStockPriceDTO));

        // 실제 메서드 실행
        StockValueResultDTO result = service.calPerValue(year, corpCode, corpName);

        // 결과 검증 (정상 처리 메시지 및 값이 설정되었는지 확인)
        assertNotNull(result);
        assertEquals("정상 처리되었습니다.", result.get결과메시지());
        assertFalse(result.get주당가치().isEmpty());
        assertFalse(result.get현재가격().isEmpty());
        assertNotNull(result.get확인시간());
    }
}