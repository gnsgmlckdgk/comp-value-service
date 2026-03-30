package com.finance.dart.cointrade.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CointradeMlModelDto {
    private Long id;
    private String coinCode;
    private String modelPath;
    private LocalDateTime trainedAt;
    private LocalDateTime trainDataStart;
    private LocalDateTime trainDataEnd;
    private BigDecimal accuracy;
    private BigDecimal aucRoc;
    private Integer featureCount;
    private Integer trainSamples;
    private String candleUnit;
    private String modelType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
