package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.TenorConfigProperties;
import com.github.kusoroadeolu.revgif.configprops.WebClientConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;
import com.github.kusoroadeolu.revgif.dtos.events.EventErrorType;
import com.github.kusoroadeolu.revgif.dtos.events.GifSearchErrorEvent;
import com.github.kusoroadeolu.revgif.dtos.gif.BatchNormalizedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.BatchTenorGif;
import com.github.kusoroadeolu.revgif.mappers.GifMapper;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.services.GifClient;
import com.github.kusoroadeolu.revgif.services.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;


@Service
@Slf4j
@RequiredArgsConstructor
public class TenorGifClient implements GifClient {
    private final TenorConfigProperties tenorConfigProperties;
    private final WebClient tenorWebClient;
    private final ApplicationEventPublisher eventPublisher;
    private final LogMapper logMapper;
    private final GifMapper gifMapper;
    private final SseService sseService;
    private final WebClientConfigProperties webClientConfigProperties;
    private final static String CLASS_NAME = TenorGifClient.class.getSimpleName();

    @Override
    public void getGifs(ImageClientResponse imageClientResponse, String session){
        final int maxRetry = this.webClientConfigProperties.maxRetryAttempts();
        final int backOff = this.webClientConfigProperties.backoff();
        final int numOfEvents = this.sseService.getExpectedEvents(session);
        String limit = this.tenorConfigProperties.limit();
        if(numOfEvents > 2){
            limit = this.tenorConfigProperties.limitForMany();
        }
        final var queryParams = buildQueryParams(imageClientResponse, limit);

        this.tenorWebClient.get()
                .uri(uriBuilder -> uriBuilder.queryParams(queryParams).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BatchTenorGif.class)
                .map(bt -> {
                    final BatchNormalizedGif bng = this.gifMapper.gifResults(bt, imageClientResponse, session);
                    log.info(this.logMapper.log(CLASS_NAME, "Tenor result for query %s: %s".formatted(imageClientResponse.searchQuery(), bng.toString())));
                    return bng;
                })
                .doOnError(e -> log.error(this.logMapper.log(CLASS_NAME, "Tenor API call failed for query: %s".formatted(imageClientResponse.searchQuery())), e))
                .onErrorResume(WebClientResponseException.class, _ -> Mono.empty())
                .retryWhen(Retry.backoff(maxRetry, Duration.ofSeconds(backOff))) //TODO extract to config
                .subscribe(this.eventPublisher::publishEvent, e -> {
                    log.error("An unexpected error occurred during tenor call", e);
                    this.eventPublisher.publishEvent(new GifSearchErrorEvent("Failed to fetch gifs from tenor", EventErrorType.TENOR_API_FAIL, session ,LocalDateTime.now()));
                }); //publish event on subscribe
    }

    @NotNull
    private MultiValueMap<String, String> buildQueryParams(ImageClientResponse imageClientResponse, String limit) {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("q", imageClientResponse.searchQuery());
        queryParams.add("key", this.tenorConfigProperties.apiKey());
        queryParams.add("contentfilter", this.tenorConfigProperties.contentFilter());
        queryParams.add("mediafilter", this.tenorConfigProperties.mediaFilter());
        queryParams.add("limit", limit);
        return queryParams;
    }

}
