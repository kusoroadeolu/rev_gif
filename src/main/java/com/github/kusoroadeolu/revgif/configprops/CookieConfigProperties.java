package com.github.kusoroadeolu.revgif.configprops;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("cookie")
public record CookieConfigProperties(
        int maxAge, boolean httpOnly, boolean secure, String name, String path
) {
}
