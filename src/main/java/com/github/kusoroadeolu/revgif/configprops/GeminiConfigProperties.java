package com.github.kusoroadeolu.revgif.configprops;


import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;


@ConfigurationProperties("api.gemini")
public record GeminiConfigProperties(
        String apiKey,
        String prompt,
        String model,
        int retryAttempts,
        List<Integer> statusCodes,
        double initialDelay,
        double maxDelay,
        int maxConns,
        int maxConnsPerHost
) {

}
