package com.atlas.externalAPIs.apiFootball.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "apis.football")
@Data
public class ApiFootballConfig {
    private String baseUrl;
    private String apiKey;
    private String apiHost;
    private int timeoutSeconds = 30;
}
