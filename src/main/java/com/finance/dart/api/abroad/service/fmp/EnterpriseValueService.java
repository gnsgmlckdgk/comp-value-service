package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.enterprisevalues.EnterpriseValuesReqDto;
import com.finance.dart.api.abroad.dto.fmp.enterprisevalues.EnterpriseValuesResDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class EnterpriseValueService {

    private final FmpClientComponent fmpClientComponent;


    /**
     * 기업가치 조회
     * @param reqDto
     * @return
     */
    public List<EnterpriseValuesResDto> findEnterpriseValue(EnterpriseValuesReqDto reqDto) {

         List<EnterpriseValuesResDto> response = fmpClientComponent.sendGet(
                 FmpApiList.EnterpriseValues,
                 reqDto,
                 new ParameterizedTypeReference<>() {}
         );

         return response;
    }

}
