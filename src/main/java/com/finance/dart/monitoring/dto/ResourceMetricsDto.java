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
public class ResourceMetricsDto {

    private List<ContainerMetric> containers;
    private GpuMetric gpu;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContainerMetric {
        private String name;
        private double cpuPercent;
        private double cpuLimitCores;
        private long memoryMB;
        private long memoryLimitMB;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GpuMetric {
        private double utilPercent;
        private long memUsedMB;
        private long memTotalMB;
    }
}
