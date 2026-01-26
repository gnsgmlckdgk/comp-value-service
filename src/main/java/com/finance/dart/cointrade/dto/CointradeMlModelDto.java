package com.finance.dart.cointrade.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CointradeMlModelDto {
    private Long id;
    private String coinCode;
    private String modelPath;
    private LocalDateTime trainedAt;
    private LocalDate trainDataStart;
    private LocalDate trainDataEnd;
    private BigDecimal mseHigh;
    private BigDecimal mseLow;
    private BigDecimal mseUpProb;
    private Integer predictionDays;
    private String modelType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
