package com.finance.dart.common.service;

import lombok.AllArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * 환경변수
 */
@Component
@AllArgsConstructor
public class ConfigService {

    private final Environment env;

    /**
     * OPEN DART API KEY
     * @return
     */
    public String getDartApiKey() {
        return env.getProperty("OPEN_DART_API_KEY");
    }

    /**
     * FMP API KEY
     * @return
     */
    public String getFmpApiKey() {
        return env.getProperty("FMP_API_KEY");
    }
}
