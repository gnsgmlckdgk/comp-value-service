package com.finance.dart.common.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class XmlUtil {

    /**
     * Xml File 문자열 반환
     * @param zipFile
     * @return
     */
    public static String getXmlContentOfZipFile(byte[] zipFile) {

        String xmlContent = "";

        try (ByteArrayInputStream bis = new ByteArrayInputStream(zipFile);
             ZipInputStream zis = new ZipInputStream(bis)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                // XML 내용을 문자열로 변환
                xmlContent = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
            }

        } catch (IOException e) {
//            log.error("ZIP 파일 처리 중 오류 발생", e);
        } finally {
            return xmlContent;
        }

    }

}
