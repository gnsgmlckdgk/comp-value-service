package com.finance.dart.board.service;

import com.finance.dart.board.config.FileStorageConfig;
import com.finance.dart.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class FileStorageService {

    private final FileStorageConfig fileStorageConfig;

    /**
     * 파일 저장
     * @return 저장된 파일명 (UUID + 확장자)
     */
    public String storeFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String storedFilename = UUID.randomUUID() + extension;

        Path targetLocation = fileStorageConfig.getUploadPath().resolve(storedFilename).normalize();

        // 경로 조작 방지
        if (!targetLocation.startsWith(fileStorageConfig.getUploadPath())) {
            throw new BizException("잘못된 파일 경로입니다.");
        }

        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BizException("파일 저장에 실패했습니다: " + originalFilename);
        }

        return storedFilename;
    }

    /**
     * 파일 로드 (다운로드용)
     */
    public Resource loadFile(String storedFilename) {
        Path filePath = fileStorageConfig.getUploadPath().resolve(storedFilename).normalize();

        // 경로 조작 방지
        if (!filePath.startsWith(fileStorageConfig.getUploadPath())) {
            throw new BizException("잘못된 파일 경로입니다.");
        }

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new BizException("파일을 찾을 수 없습니다: " + storedFilename);
            }
        } catch (MalformedURLException e) {
            throw new BizException("파일을 찾을 수 없습니다: " + storedFilename);
        }
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String storedFilename) {
        Path filePath = fileStorageConfig.getUploadPath().resolve(storedFilename).normalize();

        // 경로 조작 방지
        if (!filePath.startsWith(fileStorageConfig.getUploadPath())) {
            return;
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}", storedFilename, e);
        }
    }
}
