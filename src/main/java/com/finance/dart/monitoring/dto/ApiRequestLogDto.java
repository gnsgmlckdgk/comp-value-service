package com.finance.dart.monitoring.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequestLogDto {
    private String method;      // GET, POST, PUT, DELETE
    private String uri;         // /api/stocks, /api/cointrade/holdings
    private int status;         // 200, 404, 500
    private long durationMs;    // 응답 시간 ms
    private long timestamp;     // epoch millis
}
