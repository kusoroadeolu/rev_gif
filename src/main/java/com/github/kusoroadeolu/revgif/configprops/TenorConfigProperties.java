package com.github.kusoroadeolu.revgif.configprops;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("api.tenor")
public record TenorConfigProperties(String apiKey,
                                    String baseUrl,
                                    String contentFilter,
                                    String mediaFilter,
                                    String limit,
                                    String limitForMany
                                    )
{
}
