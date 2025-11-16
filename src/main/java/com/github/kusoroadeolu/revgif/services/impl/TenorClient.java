package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.TenorConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;
import com.github.kusoroadeolu.revgif.dtos.gif.BatchTenorGif;
import com.github.kusoroadeolu.revgif.mappers.GifMapper;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.services.GifClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;


@Service
@Slf4j
@RequiredArgsConstructor
public class TenorClient implements GifClient {
    private final TenorConfigProperties tenorConfigProperties;
    private final WebClient tenorWebClient;
    private final ApplicationEventPublisher eventPublisher;
    private final LogMapper logMapper;
    private final GifMapper gifMapper;
    private final static String CLASS_NAME = TenorClient.class.getSimpleName();

    @Override
    public void getGifs(ImageClientResponse imageClientResponse){
        final var queryParams = buildQueryParams(imageClientResponse);
        this.tenorWebClient.get()
                .uri(uriBuilder -> uriBuilder.queryParams(queryParams).build())
                .retrieve()
                .bodyToMono(BatchTenorGif.class)
                .map(bt -> {
                    var nm = this.gifMapper.gifResults(bt, imageClientResponse);
                    log.info(this.logMapper.log(
                            CLASS_NAME,
                            "Tenor result for query %s: %s".formatted(imageClientResponse.searchQuery(), nm.toString())
                    ));
                    this.eventPublisher.publishEvent(nm);
                    return bt;
                })
                .doOnError(e -> log.error(this.logMapper.log(
                                CLASS_NAME,
                                "Tenor API call failed for query: %s".formatted(imageClientResponse.searchQuery())
                                ), e
                        )
                )
                .onErrorResume(WebClientResponseException.class, e -> {
                    log.warn(this.logMapper.log(
                            CLASS_NAME,
                            "Returning empty result due to Tenor API failure for query %s".formatted(imageClientResponse.searchQuery())),
                            e
                    );
                    return Mono.empty();
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(2)))
                .subscribe();
    }

    @NotNull
    private MultiValueMap<String, String> buildQueryParams(ImageClientResponse imageClientResponse) {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("q", imageClientResponse.searchQuery());
        queryParams.add("key", this.tenorConfigProperties.apiKey());
        queryParams.add("contentfilter", this.tenorConfigProperties.contentFilter());
        queryParams.add("mediafilter", this.tenorConfigProperties.mediaFilter());
        queryParams.add("limit", this.tenorConfigProperties.limit());
        return queryParams;
    }

}
