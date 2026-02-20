package com.finance.dart.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStatusDto {

    private String name;       // BUY, SELL
    private String status;     // RUNNING, IDLE, ERROR
    private int percent;       // 0-100
    private String message;
}
