package com.finance.dart.api.common.service;

import com.finance.dart.api.common.dto.log.LogFileInfoDto;
import com.finance.dart.api.common.dto.log.LogFileListDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
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
}