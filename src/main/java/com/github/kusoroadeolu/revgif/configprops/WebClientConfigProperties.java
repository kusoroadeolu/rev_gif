package com.github.kusoroadeolu.revgif.configprops;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("web-client")
public record WebClientConfigProperties(
        int sslHandShakeTimeout,
        int responseTimeout,
        int readWriteTimeout,
        int tcpTimeoutMillis,
        int maxBytesRead,
        int maxRetryAttempts,
        int backoff
) {
}
