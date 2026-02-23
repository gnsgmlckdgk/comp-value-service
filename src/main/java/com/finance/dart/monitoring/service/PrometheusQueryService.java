package com.finance.dart.monitoring.service;

import com.finance.dart.monitoring.dto.ResourceMetricsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
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
            // CPU 사용률 (pod별 합산, container="" 제외 — POD cgroup 중복 방지)
            Map<String, Double> cpuMap = queryVector(
                    "sum by (pod) (irate(container_cpu_usage_seconds_total{namespace=\"" + namespace + "\",pod!=\"\",container!=\"\"}[1m])) * 100",
                    "pod"
            );

            // Memory 사용량 (pod별 합산)
            Map<String, Double> memMap = queryVector(
                    "sum by (pod) (container_memory_working_set_bytes{namespace=\"" + namespace + "\",pod!=\"\",container!=\"\"})",
                    "pod"
            );

            // Memory limit (pod별 합산)
            Map<String, Double> memLimitMap = queryVector(
                    "sum by (pod) (kube_pod_container_resource_limits{namespace=\"" + namespace + "\",resource=\"memory\",pod!=\"\"})",
                    "pod"
            );

            // pod별로 합치기 (pod 이름에서 deployment 접미사 제거)
            for (String pod : cpuMap.keySet()) {
                String name = podToServiceName(pod);
                containers.add(ResourceMetricsDto.ContainerMetric.builder()
                        .name(name)
                        .cpuPercent(cpuMap.getOrDefault(pod, 0.0))
                        .memoryMB(Math.round(memMap.getOrDefault(pod, 0.0) / 1024 / 1024))
                        .memoryLimitMB(Math.round(memLimitMap.getOrDefault(pod, 0.0) / 1024 / 1024))
                        .build());
            }
        } catch (Exception e) {
            log.warn("Prometheus 컨테이너 메트릭 조회 실패: {}", e.getMessage());
        }

        // GPU 메트릭
        ResourceMetricsDto.GpuMetric gpuMetric = null;
        try {
            Map<String, Double> gpuUtil = queryVector("DCGM_FI_DEV_GPU_UTIL", "gpu");
            Map<String, Double> gpuMem = queryVector("DCGM_FI_DEV_FB_USED", "gpu");
            Map<String, Double> gpuMemTotal = queryVector("DCGM_FI_DEV_FB_FREE", "gpu");

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
    private Map<String, Double> queryVector(String query, String labelKey) {
        URI uri = UriComponentsBuilder.fromHttpUrl(prometheusUrl)
                .path("/api/v1/query")
                .queryParam("query", query)
                .build()
                .encode()
                .toUri();

        Map<String, Object> response = webClient.get()
                .uri(uri)
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

            String key = metric.getOrDefault(labelKey, "unknown");
            if (key.isEmpty()) continue;
            double val = Double.parseDouble(String.valueOf(value.get(1)));
            result.put(key, val);
        }

        return result;
    }

    /**
     * pod 이름에서 서비스명 추출 (deployment replicaset 접미사 제거)
     * e.g. "comp-value-service-5845fd94fd-d2wvm" → "comp-value-service"
     */
    private String podToServiceName(String pod) {
        // ReplicaSet 패턴: {name}-{replicaset-hash}-{pod-hash}
        String[] parts = pod.split("-");
        if (parts.length >= 3) {
            // 끝 2개 (replicaset hash + pod hash) 제거
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length - 2; i++) {
                if (i > 0) sb.append("-");
                sb.append(parts[i]);
            }
            return sb.toString();
        }
        return pod;
    }
}
