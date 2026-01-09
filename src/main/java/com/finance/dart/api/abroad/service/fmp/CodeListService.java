package com.finance.dart.api.abroad.service.fmp;

import com.finance.dart.api.abroad.component.FmpClientComponent;
import com.finance.dart.api.abroad.dto.fmp.FmpReqCommon;
import com.finance.dart.api.abroad.dto.fmp.codelist.CountriesDto;
import com.finance.dart.api.abroad.dto.fmp.codelist.ExchangesDto;
import com.finance.dart.api.abroad.dto.fmp.codelist.IndustiresDto;
import com.finance.dart.api.abroad.dto.fmp.codelist.SectorsDto;
import com.finance.dart.api.abroad.enums.FmpApiList;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@AllArgsConstructor
@Service
public class CodeListService {

    private final FmpClientComponent fmpClientComponent;

    /**
     * 거래소 목록조회
     * @return
     */
    public List<ExchangesDto> findExchanges() {

        List<ExchangesDto> response = fmpClientComponent.sendGet(
                FmpApiList.AvailableExchanges,
                new FmpReqCommon(),
                new ParameterizedTypeReference<>() {}
        );

        return response;
    }


    /**
     * 섹터 목록조회
     * @return
     */
    public List<SectorsDto> findSectors() {

         List<SectorsDto> response = fmpClientComponent.sendGet(
                 FmpApiList.AvailableSectors,
                 new FmpReqCommon(),
                 new ParameterizedTypeReference<>() {}
         );

         return response;
    }

    /**
     * 산업 목록조회
     * @return
     */
    public List<IndustiresDto> findIndustries() {

        List<IndustiresDto> response = fmpClientComponent.sendGet(
                FmpApiList.AvailablesIndustries,
                new FmpReqCommon(),
                new ParameterizedTypeReference<>() {}
        );

        return response;
    }

    /**
     * 국가코드 목록조회
     * @return
     */
    public List<CountriesDto> findCountries() {

        List<CountriesDto> response = fmpClientComponent.sendGet(
                FmpApiList.AvailableCountries,
                new FmpReqCommon(),
                new ParameterizedTypeReference<>() {}
        );

        return response;
    }


}
