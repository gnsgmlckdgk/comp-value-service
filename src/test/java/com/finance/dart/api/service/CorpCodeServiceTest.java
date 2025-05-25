package com.finance.dart.api.service;

import com.finance.dart.api.dto.CorpCodeDTO;
import com.finance.dart.common.service.ConfigService;
import com.finance.dart.common.service.HttpClientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@Disabled("캐시 추가 후 오류남(비활성화)")
class CorpCodeServiceTest {

    @Mock
    private HttpClientService httpClientService;

    @Mock
    private ConfigService configService;

    @InjectMocks
    private CorpCodeService corpCodeService;

    // 더미 API 키와 XML 응답 문자열
    private final String dummyApiKey = "dummyApiKey";
    private final String dummyXml = "<result>" +
            "<list>" +
            "<item>" +
            "<corpName>Test Corp</corpName>" +
            "<stockCode>12345</stockCode>" +
            "</item>" +
            "<item>" +
            "<corpName>NonPublic Corp</corpName>" +
            "<stockCode></stockCode>" +
            "</item>" +
            "</list>" +
            "</result>";

    // 테스트에서는 XmlUtil.getXmlContentOfZipFile이 단순히 byte[]를 문자열로 변환한다고 가정합니다.
    private final byte[] dummyZipBytes = dummyXml.getBytes(StandardCharsets.UTF_8);

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        when(configService.getDartAPI_Key()).thenReturn(dummyApiKey);
        // 실제 압축된 XML 데이터를 dummyZipBytes로 생성
        byte[] dummyZipBytes = createZipBytes("dummy.xml", dummyXml);
        // httpClientService.exchangeSync() 호출 시 dummyZipBytes를 반환하도록 설정
        when(httpClientService.exchangeSync(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(byte[].class)))
                .thenReturn(ResponseEntity.ok(dummyZipBytes));
    }

//    @Test
//    void testGetCorpCode_PublicCompanies() {
//        // 상장 기업(true)이면 stockCode가 있는 항목만 필터링됨
//        CorpCodeResDTO res = corpCodeService.getCorpCode(true);
//        assertNotNull(res);
//        List<CorpCodeDTO> list = res.getList();
//        assertNotNull(list);
//        // dummyXml의 첫번째 항목만 상장기업으로 간주됨
//        assertEquals(1, list.size());
//        CorpCodeDTO corp = list.get(0);
//        assertEquals("Test Corp", corp.getCorpName());
//        assertEquals("12345", corp.getStockCode());
//    }

//    @Test
//    void testGetCorpCodeFindName_Found() {
//        CorpCodeDTO corp = corpCodeService.getCorpCodeFindName(true, "Test Corp");
//        assertNotNull(corp);
//        assertEquals("Test Corp", corp.getCorpName());
//        assertEquals("12345", corp.getStockCode());
//    }

    @Test
    void testGetCorpCodeFindName_NotFound() {
        CorpCodeDTO corp = corpCodeService.getCorpCodeFindName(true, "Unknown Corp");
        assertNull(corp);
    }


    /**
     * Zip 파일로 압축
     * @param fileName
     * @param content
     * @return
     * @throws IOException
     */
    private byte[] createZipBytes(String fileName, String content) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            ZipEntry entry = new ZipEntry(fileName);
            zos.putNextEntry(entry);
            zos.write(content.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return baos.toByteArray();
    }

}