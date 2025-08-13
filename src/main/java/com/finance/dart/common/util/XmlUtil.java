package com.finance.dart.common.util;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class XmlUtil {

    /**
     * Xml File 문자열 반환
     * @param zipFile
     * @return
     */
//    public static String getXmlContentOfZipFile(byte[] zipFile) {
//
//        String xmlContent = "";
//
//        try (ByteArrayInputStream bis = new ByteArrayInputStream(zipFile);
//             ZipInputStream zis = new ZipInputStream(bis)) {
//
//            ZipEntry entry;
//            while ((entry = zis.getNextEntry()) != null) {
//                // XML 내용을 문자열로 변환
//                xmlContent = new String(zis.readAllBytes(), StandardCharsets.UTF_8);
//            }
//
//        } catch (IOException e) {
////            log.error("ZIP 파일 처리 중 오류 발생", e);
//        } finally {
//            return xmlContent;
//        }
//
//    }

    public static String getXmlContentOfZipFile(byte[] zipFile) {
        StringBuilder xmlBuilder = new StringBuilder();

        try (ByteArrayInputStream bis = new ByteArrayInputStream(zipFile);
             ZipInputStream zis = new ZipInputStream(bis)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if(log.isDebugEnabled()) log.debug("🔍 ZIP entry: " + entry.getName());

                byte[] data = zis.readAllBytes();
                if(log.isDebugEnabled()) log.debug("📦 Entry size: " + data.length);
                
                // 원하는 파일만 파싱하거나, 모든 파일 이어붙이기
                if (entry.getName().endsWith(".xml")) {
                    xmlBuilder.append(new String(data, StandardCharsets.UTF_8));
                }
            }

        } catch (IOException e) {
            e.printStackTrace(); // 임시로 보여주기
        }

        return xmlBuilder.toString();
    }

}
