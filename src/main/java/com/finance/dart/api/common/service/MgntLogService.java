package com.finance.dart.api.common.service;

import com.finance.dart.api.common.dto.log.LogContentDto;
import com.finance.dart.api.common.dto.log.LogFileInfoDto;
import com.finance.dart.api.common.dto.log.LogFileListDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MgntLogService {

    @Value("${logging.file.path:./logs}")   // 기본경로
    private String logPath;

    public LogFileListDto getLogFileList() {
        try {
            File logDir = new File(logPath);
            List<LogFileInfoDto> fileInfos = new ArrayList<>();

            if (logDir.exists() && logDir.isDirectory()) {
                File[] files = logDir.listFiles();
                if (files != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    fileInfos = Arrays.stream(files)
                            .filter(File::isFile)
                            .map(file -> {
                                Path path = Paths.get(file.getPath()).toAbsolutePath().normalize();

                                LogFileInfoDto info = new LogFileInfoDto();
                                info.setFilename(file.getName());
                                info.setSize(file.length());
                                info.setModifiedAt(sdf.format(new Date(file.lastModified())));
                                info.setPath(path.toString());
                                return info;
                            })
                            .sorted(Comparator.comparing(LogFileInfoDto::getModifiedAt).reversed())
                            .collect(Collectors.toList());
                }
            } else {
                log.warn("로그 디렉토리를 찾을 수 없습니다: {}", logPath);
            }

            LogFileListDto body = new LogFileListDto();
            body.setFiles(fileInfos);

            if (log.isDebugEnabled()) {
                log.debug("로그 파일 목록 조회 완료 - count: {}", fileInfos.size());
            }

            return body;

        } catch (Exception e) {
            log.error("로그 파일 목록 조회 실패 - error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get log file list", e);
        }
    }

    /**
     * 로그파일 내용 조회
     * @param filename
     * @return
     */
    public LogContentDto getLogContent(String filename) {
        try {
            File logDir = new File(logPath);
            File file = new File(logDir, filename);

            // 보안 체크: 경로 탈출 방지
            String canonicalPath = file.getCanonicalPath();
            String canonicalLogPath = logDir.getCanonicalPath();
            if (!canonicalPath.startsWith(canonicalLogPath)) {
                log.error("보안 위반 시도: 허용되지 않은 경로 접근 - filename: {}", filename);
                throw new SecurityException("잘못된 파일 경로 접근입니다.");
            }

            if (!file.exists() || !file.isFile()) {
                log.warn("파일을 찾을 수 없습니다: {}", file.getAbsolutePath());
                throw new IllegalArgumentException("파일을 찾을 수 없습니다: " + filename);
            }

            // 전체 내용 읽기
            String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
            int totalLines = (int) content.lines().count();

            LogContentDto dto = new LogContentDto();
            dto.setFilename(filename);
            dto.setContent(content);
            dto.setLines(totalLines);
            dto.setTotalLines(totalLines);
            dto.setStartLine(1);
            dto.setEndLine(totalLines);
            dto.setHasMore(false);

            if (log.isDebugEnabled()) {
                log.debug("로그 파일 내용 조회 완료 - filename: {}, size: {} chars", filename, content.length());
            }

            return dto;

        } catch (IOException e) {
            log.error("로그 파일 읽기 실패 - filename: {}, error: {}", filename, e.getMessage(), e);
            throw new RuntimeException("로그 파일을 읽는 중 오류가 발생했습니다: " + filename, e);
        }
    }

}