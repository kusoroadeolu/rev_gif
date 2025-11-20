package com.github.kusoroadeolu.revgif.configprops;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rate-limit")
public record RateLimitConfigProperties(
        int reqPerMinute,
        int keyTtl
) {
}
