package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.forexquote.ForexQuoteReqDto;
import com.finance.dart.api.abroad.dto.fmp.forexquote.ForexQuoteResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 외환시세 API 서비스
 */
@AllArgsConstructor
@Service
public class ForexQuoteService {

    private final FmpClientComponent fmpClientComponent;


    public List<ForexQuoteResDto> findForexQuote(ForexQuoteReqDto reqDto) {

        List<ForexQuoteResDto> response = fmpClientComponent.sendGet(
                FmpApiList.ForexQuote,
                reqDto,
                new ParameterizedTypeReference<>() {}
        );

        return response;

    }

}
