package com.finance.dart.monitoring.service;

import com.finance.dart.monitoring.dto.ResourceMetricsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Prometheus HTTP API를 통해 컨테이너/GPU 리소스 메트릭 수집
 */
@Slf4j
@Service
public class PrometheusQueryService {

    private final WebClient webClient;
    private final String prometheusUrl;
    private final boolean enabled;
    private final String namespace;

    public PrometheusQueryService(
            WebClient webClient,
            @Value("${monitoring.prometheus.url:http://prometheus.monitoring.svc.cluster.local:9090}") String prometheusUrl,
            @Value("${monitoring.prometheus.enabled:false}") boolean enabled,
            @Value("${monitoring.prometheus.namespace:comp-value}") String namespace
    ) {
        this.webClient = webClient;
        this.prometheusUrl = prometheusUrl;
        this.enabled = enabled;
        this.namespace = namespace;
    }

    public ResourceMetricsDto queryMetrics() {
        if (!enabled) {
            return ResourceMetricsDto.builder()
                    .containers(List.of())
                    .gpu(null)
                    .build();
        }

        List<ResourceMetricsDto.ContainerMetric> containers = new ArrayList<>();

        try {
            // CPU 사용률
            Map<String, Double> cpuMap = queryVector(
                    "rate(container_cpu_usage_seconds_total{namespace=\"" + namespace + "\",container!=\"\"}[5m]) * 100"
            );

            // Memory 사용량
            Map<String, Double> memMap = queryVector(
                    "container_memory_working_set_bytes{namespace=\"" + namespace + "\",container!=\"\"}"
            );

            // Memory limit
            Map<String, Double> memLimitMap = queryVector(
                    "container_spec_memory_limit_bytes{namespace=\"" + namespace + "\",container!=\"\"}"
            );

            // 컨테이너별로 합치기
            for (String container : cpuMap.keySet()) {
                containers.add(ResourceMetricsDto.ContainerMetric.builder()
                        .name(container)
                        .cpuPercent(cpuMap.getOrDefault(container, 0.0))
                        .memoryMB(Math.round(memMap.getOrDefault(container, 0.0) / 1024 / 1024))
                        .memoryLimitMB(Math.round(memLimitMap.getOrDefault(container, 0.0) / 1024 / 1024))
                        .build());
            }
        } catch (Exception e) {
            log.debug("Prometheus 컨테이너 메트릭 조회 실패: {}", e.getMessage());
        }

        // GPU 메트릭
        ResourceMetricsDto.GpuMetric gpuMetric = null;
        try {
            Map<String, Double> gpuUtil = queryVector("DCGM_FI_DEV_GPU_UTIL");
            Map<String, Double> gpuMem = queryVector("DCGM_FI_DEV_FB_USED");
            Map<String, Double> gpuMemTotal = queryVector("DCGM_FI_DEV_FB_FREE");

            if (!gpuUtil.isEmpty()) {
                double util = gpuUtil.values().iterator().next();
                long memUsed = Math.round(gpuMem.values().stream().findFirst().orElse(0.0));
                long memFree = Math.round(gpuMemTotal.values().stream().findFirst().orElse(0.0));

                gpuMetric = ResourceMetricsDto.GpuMetric.builder()
                        .utilPercent(util)
                        .memUsedMB(memUsed)
                        .memTotalMB(memUsed + memFree)
                        .build();
            }
        } catch (Exception e) {
            log.debug("Prometheus GPU 메트릭 조회 실패: {}", e.getMessage());
        }

        return ResourceMetricsDto.builder()
                .containers(containers)
                .gpu(gpuMetric)
                .build();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Double> queryVector(String query) {
        Map<String, Object> response = webClient.get()
                .uri(prometheusUrl + "/api/v1/query?query={query}", query)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(5))
                .block();

        Map<String, Double> result = new java.util.LinkedHashMap<>();
        if (response == null) return result;

        Map<String, Object> data = (Map<String, Object>) response.get("data");
        if (data == null) return result;

        List<Map<String, Object>> results = (List<Map<String, Object>>) data.get("result");
        if (results == null) return result;

        for (Map<String, Object> item : results) {
            Map<String, String> metric = (Map<String, String>) item.get("metric");
            List<Object> value = (List<Object>) item.get("value");

            String containerName = metric.getOrDefault("container",
                    metric.getOrDefault("gpu", "unknown"));
            double val = Double.parseDouble(String.valueOf(value.get(1)));
            result.put(containerName, val);
        }

        return result;
    }
}
