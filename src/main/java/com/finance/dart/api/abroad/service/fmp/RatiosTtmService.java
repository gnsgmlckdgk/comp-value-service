package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.ratiosttm.RatiosTtmArrReqDto;
import com.finance.dart.api.abroad.dto.fmp.ratiosttm.RatiosTtmReqDto;
import com.finance.dart.api.abroad.dto.fmp.ratiosttm.RatiosTtmResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import com.finance.dart.common.component.ConfigComponent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@AllArgsConstructor
@Service
public class RatiosTtmService {

    private final FmpClientComponent fmpClientComponent;
    private final ConfigComponent configComponent;


    /**
     * 최근 12개월 재무 비율 조회
     * @param reqDto
     * @return
     */
    public List<RatiosTtmResDto> findRatiosTTM(RatiosTtmReqDto reqDto) {

        List<RatiosTtmResDto> response = fmpClientComponent.sendGet(
                FmpApiList.RatiosTTM,
                reqDto,
                new ParameterizedTypeReference<>() {}
        );

        return response;
    }

    /**
     * 최근 12개월 재무 비율 조회 (다건)
     * @param reqDto
     * @return
     */
    public Map<String, List<RatiosTtmResDto>> findRatiosTTM(RatiosTtmArrReqDto reqDto) {

        List<String> symbols = reqDto.getSymbol();

        List<RatiosTtmReqDto> ratiosTtmReqList = symbols.stream()
                .map(RatiosTtmReqDto::new)
                .toList();

        Map<String, List<RatiosTtmResDto>> response =
                fmpClientComponent.sendGetParallel(
                        FmpApiList.RatiosTTM,
                        ratiosTtmReqList,
                        RatiosTtmReqDto::getSymbol,
                        new ParameterizedTypeReference<>() {});

        return response;
    }

}
