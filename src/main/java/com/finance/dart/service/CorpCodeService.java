package com.finance.dart.service;

import com.finance.dart.common.service.ConfigService;
import com.finance.dart.common.service.HttpClientService;
import com.finance.dart.common.util.ClientUtil;
import com.finance.dart.common.util.XmlUtil;
import com.finance.dart.dto.CorpCodeDTO;
import com.finance.dart.dto.CorpCodeResDTO;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * 공시정보 > 고유번호 조회 서비스
 */

@Slf4j
@Service
@AllArgsConstructor
public class CorpCodeService {

    private final HttpClientService httpClientService;
    private final ConfigService configService;


    /**
     * 고유번호 조회
     * @param publicCompanyYn 상장기업 여부
     * @return
     */
    public CorpCodeResDTO getCorpCode(boolean publicCompanyYn) {

        final String API_KEY = configService.getDartAPI_Key();

        HttpEntity entity = ClientUtil.createHttpEntity(MediaType.APPLICATION_XML);
        String url = "https://opendart.fss.or.kr/api/corpCode.xml?crtfc_key="+API_KEY;

        ResponseEntity<byte[]> response = httpClientService.exchangeSync(url, HttpMethod.GET, entity, byte[].class);
        byte[] zipFile = response.getBody();

        return getZipData(zipFile, publicCompanyYn);
    }

    /**
     * 기업명으로 기업정보 검색
     * @param publicCompanyYn
     * @param corpName
     * @return
     */
    public CorpCodeDTO getCorpCodeFindName(boolean publicCompanyYn, String corpName) {

        final String API_KEY = configService.getDartAPI_Key();

        HttpEntity entity = ClientUtil.createHttpEntity(MediaType.APPLICATION_XML);
        String url = "https://opendart.fss.or.kr/api/corpCode.xml?crtfc_key="+API_KEY;

        ResponseEntity<byte[]> response = httpClientService.exchangeSync(url, HttpMethod.GET, entity, byte[].class);
        byte[] zipFile = response.getBody();

        CorpCodeResDTO corpCodeResDTO = getZipData(zipFile, publicCompanyYn);
        List<CorpCodeDTO> corpCodeDTOList = corpCodeResDTO.getList();

        for(CorpCodeDTO corpCodeDTO : corpCodeDTOList) {
            String cn = corpCodeDTO.getCorpName();
            if(corpName.equals(cn)) {
                return corpCodeDTO;
            }
        }

        return null;
    }

    private CorpCodeResDTO getZipData(byte[] zipFile, boolean publicCompanyYn) {

        String xmlContent = XmlUtil.getXmlContentOfZipFile(zipFile);

        JSONObject jsonObject = XML.toJSONObject(xmlContent).getJSONObject("result");
        CorpCodeResDTO corpCodeResDTO = new Gson().fromJson(jsonObject.toString(), CorpCodeResDTO.class);

//        log.info("DART에 등록된 전체 기업수 = {}", corpCodeResDTO.getList().size());

        // 상장 기업만
        if(publicCompanyYn) {
            List<CorpCodeDTO> corpList = corpCodeResDTO.getList().stream()
                    .filter(corp -> !corp.getStockCode().equals(""))
                    .toList();
//            log.info("DART에 등록된 상장 기업수 = {}", corpList.size());
            corpCodeResDTO.setList(corpList);
        }

        return corpCodeResDTO;
    }


}
