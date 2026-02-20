package com.finance.dart.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceStatusDto {

    private String name;
    private String status; // UP, DOWN, DEGRADED
    private String uptime;
    private String version;
}
