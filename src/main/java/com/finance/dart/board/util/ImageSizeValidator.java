package com.finance.dart.board.util;

import com.finance.dart.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 게시글 이미지 크기 검증 유틸리티
 */
@Slf4j
public class ImageSizeValidator {

    // 개별 이미지 최대 크기: 5MB (일반적인 웹 게시판 기준)
    private static final long MAX_SINGLE_IMAGE_SIZE = 5 * 1024 * 1024; // 5MB
//    private static final long MAX_SINGLE_IMAGE_SIZE = 1 * 100 * 1024; // 100KB 테스트

    // 전체 데이터 최대 크기: 10MB (게시글 본문 + 모든 이미지 포함)
    private static final long MAX_TOTAL_DATA_SIZE = 10 * 1024 * 1024; // 10MB
//    private static final long MAX_TOTAL_DATA_SIZE = 1 * 100 * 1024; // 100KB 테스트

    // Base64 이미지 패턴: data:image/...;base64,xxxxx
    private static final Pattern BASE64_IMAGE_PATTERN =
        Pattern.compile("data:image/[^;]+;base64,([^\"'\\s>]+)", Pattern.CASE_INSENSITIVE);

    /**
     * 게시글 내용의 이미지 크기를 검증
     * @param content 게시글 내용 (HTML)
     * @throws BizException 크기 제한 초과 시
     */
    public static void validateImageSize(String content) {
        if (content == null || content.isEmpty()) {
            return;
        }

        // 전체 컨텐츠 크기 체크 (본문 텍스트 + 모든 이미지) - 이미지 유무와 관계없이 항상 체크
        long contentSize = content.getBytes().length;
        if (contentSize > MAX_TOTAL_DATA_SIZE) {
            double totalSizeMB = (double) contentSize / (1024 * 1024);
            double maxMB = (double) MAX_TOTAL_DATA_SIZE / (1024 * 1024);
            throw new BizException(
                String.format("전체 게시글 크기가 너무 큽니다. (%.2fMB / 최대 %.0fMB)",
                    totalSizeMB, maxMB)
            );
        }

        // Base64 이미지 추출
        List<String> base64Images = extractBase64Images(content);

        if (base64Images.isEmpty()) {
            log.debug("이미지 크기 검증 완료 - 이미지 수: 0, 전체 크기: {}MB",
                String.format("%.2f", (double) contentSize / (1024 * 1024)));
            return; // 이미지가 없으면 개별 이미지 검증 생략
        }

        int imageCount = 0;

        // 각 이미지 크기 검증
        for (String base64Data : base64Images) {
            imageCount++;

            // Base64 디코딩하여 실제 바이트 크기 계산
            long imageSize = calculateBase64Size(base64Data);

            // 개별 이미지 크기 체크
            if (imageSize > MAX_SINGLE_IMAGE_SIZE) {
                double sizeMB = (double) imageSize / (1024 * 1024);
                double maxMB = (double) MAX_SINGLE_IMAGE_SIZE / (1024 * 1024);
                throw new BizException(
                    String.format("이미지 크기가 너무 큽니다. (이미지 %d: %.2fMB / 최대 %.0fMB)",
                        imageCount, sizeMB, maxMB)
                );
            }
        }

        log.debug("이미지 크기 검증 완료 - 이미지 수: {}, 전체 크기: {}MB",
            imageCount, String.format("%.2f", (double) contentSize / (1024 * 1024)));
    }

    /**
     * HTML 컨텐츠에서 Base64 이미지 데이터 추출
     */
    private static List<String> extractBase64Images(String content) {
        List<String> images = new ArrayList<>();
        Matcher matcher = BASE64_IMAGE_PATTERN.matcher(content);

        while (matcher.find()) {
            String base64Data = matcher.group(1);
            images.add(base64Data);
        }

        return images;
    }

    /**
     * Base64 문자열의 실제 바이트 크기 계산
     * Base64는 원본의 약 4/3 크기이므로 디코딩하여 정확한 크기 계산
     */
    private static long calculateBase64Size(String base64Data) {
        try {
            // Base64 디코딩하여 실제 바이트 배열 크기 반환
            byte[] decodedBytes = Base64.getDecoder().decode(base64Data);
            return decodedBytes.length;
        } catch (IllegalArgumentException e) {
            log.warn("Base64 디코딩 실패, 문자열 길이로 추정: {}", e.getMessage());
            // 디코딩 실패 시 Base64 문자열 길이 기준으로 추정 (3/4 비율)
            return (long) (base64Data.length() * 0.75);
        }
    }

    /**
     * 최대 개별 이미지 크기 반환 (MB)
     */
    public static double getMaxSingleImageSizeMB() {
        return (double) MAX_SINGLE_IMAGE_SIZE / (1024 * 1024);
    }

    /**
     * 최대 전체 데이터 크기 반환 (MB)
     */
    public static double getMaxTotalDataSizeMB() {
        return (double) MAX_TOTAL_DATA_SIZE / (1024 * 1024);
    }
}
