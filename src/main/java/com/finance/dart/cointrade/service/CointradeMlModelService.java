package com.finance.dart.cointrade.service;

import com.finance.dart.cointrade.dto.CointradeMlModelDto;
import com.finance.dart.cointrade.repository.CointradeMlModelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 코인 자동매매 ML 모델 정보 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CointradeMlModelService {

    private final CointradeMlModelRepository cointradeMlModelRepository;

    /**
     * ML 모델 전체 목록 조회 (최근 수정순)
     */
    @Transactional(readOnly = true)
    public List<CointradeMlModelDto> getAllModels() {
        return cointradeMlModelRepository.findAll(Sort.by(Sort.Direction.DESC, "updatedAt")).stream()
                .map(entity -> CointradeMlModelDto.builder()
                        .id(entity.getId())
                        .coinCode(entity.getCoinCode())
                        .modelPath(entity.getModelPath())
                        .trainedAt(entity.getTrainedAt())
                        .trainDataStart(entity.getTrainDataStart())
                        .trainDataEnd(entity.getTrainDataEnd())
                        .mseHigh(entity.getMseHigh())
                        .mseLow(entity.getMseLow())
                        .lossUpProb(entity.getLossUpProb())
                        .predictionDays(entity.getPredictionDays())
                        .modelType(entity.getModelType())
                        .createdAt(entity.getCreatedAt())
                        .updatedAt(entity.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
