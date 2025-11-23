package com.github.kusoroadeolu.revgif.configprops;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Profile;

@Profile("prod")
@ConfigurationProperties("spring.data.redis.cloud")
public record RedisCloudConfigProperties(
        String host,
        int port,
        String username,
        String password
) {
}
