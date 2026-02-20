package com.finance.dart.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitoringSnapshotDto {

    private List<ServiceStatusDto> services;
    private ProcessStatusDto buyProcess;
    private ProcessStatusDto sellProcess;
    private ResourceMetricsDto resources;
    private int holdingsCount;
    private int todayTradeCount;
    private long timestamp;
}
