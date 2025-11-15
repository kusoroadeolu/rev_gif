package com.github.kusoroadeolu.revgif.config;

import com.github.kusoroadeolu.revgif.configprops.GeminiConfigProperties;
import com.github.kusoroadeolu.revgif.configprops.TenorConfigProperties;
import com.google.genai.Client;
import com.google.genai.types.ClientOptions;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebConfig {

    private final GeminiConfigProperties geminiConfigProperties;
    private final TenorConfigProperties tenorConfigProperties;

    @Bean
    public WebClient tenorClient(){
        return WebClient.create(this.tenorConfigProperties.baseUrl());
    }

    @Bean
    public Client geminiClient(){
        HttpOptions options = HttpOptions.builder()
                .retryOptions(
                        HttpRetryOptions
                                .builder()
                                .attempts(this.geminiConfigProperties.retryAttempts())
                                .httpStatusCodes(this.geminiConfigProperties.statusCodes())
                                .initialDelay(this.geminiConfigProperties.initialDelay())
                                .maxDelay(this.geminiConfigProperties.maxDelay())
                                .build()
                )
                .build();

        ClientOptions clientOptions = ClientOptions.builder()
                .maxConnections(this.geminiConfigProperties.maxConns())
                .maxConnectionsPerHost(this.geminiConfigProperties.maxConnsPerHost())
                .build();

        return Client.builder()
                .clientOptions(clientOptions)
                .httpOptions(options)
                .apiKey(this.geminiConfigProperties.apiKey())
                .build();
    }

}
