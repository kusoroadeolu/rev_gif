package com.github.kusoroadeolu.revgif.config;

import com.github.kusoroadeolu.revgif.configprops.GeminiConfigProperties;
import com.github.kusoroadeolu.revgif.configprops.TenorConfigProperties;
import com.google.genai.Client;
import com.google.genai.types.ClientOptions;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.HttpRetryOptions;
import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import lombok.RequiredArgsConstructor;
import okhttp3.internal.connection.Exchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class WebConfig {

    private final GeminiConfigProperties geminiConfigProperties;
    private final TenorConfigProperties tenorConfigProperties;

    @Bean
    public WebClient tenorWebClient() throws SSLException{
        final SslContext ctx = SslContextBuilder.forClient().build();
        final HttpClient httpClient =
                HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(30))
                        .secure(spec -> spec.sslContext(ctx))
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20_000);

        return WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(this.tenorConfigProperties.baseUrl())
                .build();

    }

    @Bean
    public WebClient urlDownloadWebClient(ExchangeStrategies exchangeStrategies){
        final HttpClient httpClient =
                HttpClient.create()
                        .responseTimeout(Duration.ofSeconds(30))
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 20_000);

        return WebClient
                .builder()
                .exchangeStrategies(exchangeStrategies)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();

    }

    @Bean
    public ExchangeStrategies exchangeStrategies(){
        return ExchangeStrategies
                .builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .build();
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
