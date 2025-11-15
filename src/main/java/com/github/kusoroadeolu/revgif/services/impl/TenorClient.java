package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.TenorConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;
import com.github.kusoroadeolu.revgif.dtos.gif.BatchTenorGifResult;
import com.github.kusoroadeolu.revgif.mappers.GifMapper;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.services.GifClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;


@Service
@Slf4j
@RequiredArgsConstructor
public class TenorClient implements GifClient {
    private final TenorConfigProperties tenorConfigProperties;
    private final WebClient tenorClient;
    private final LogMapper logMapper;
    private final GifMapper gifMapper;
    private final static String CLASS_NAME = TenorClient.class.getSimpleName();

    @Override
    public void getGifs(ImageClientResponse imageClientResponse){
        final var queryParams = buildQueryParams(imageClientResponse);
        this.tenorClient.get()
                .uri(uriBuilder -> {
                     return uriBuilder.queryParams(queryParams).build();
                })
                .retrieve()
                .bodyToMono(BatchTenorGifResult.class)
                .map(bt -> {
                    this.gifMapper.gifResults(bt, imageClientResponse);
                    return bt;
                })
                .doOnError(e -> log.error(this.logMapper.getLog(
                                CLASS_NAME,
                                "Tenor API call failed for query: %s".formatted(imageClientResponse.searchQuery())
                                ), e
                        )
                )
                .onErrorResume(e -> {
                    log.warn(this.logMapper.getLog(
                            CLASS_NAME,
                            "Returning empty result due to Tenor API failure for query %s".formatted(imageClientResponse.searchQuery())).toString()
                    );
                    return Mono.empty();
                })
                .subscribe(
                        result ->  log.info(this.logMapper.getLog(
                                CLASS_NAME,
                                "Tenor result for query {}: {}".formatted(imageClientResponse.searchQuery(), result)
                                )
                        )
                );
    }

    @NotNull
    private MultiValueMap<String, String> buildQueryParams(ImageClientResponse imageClientResponse) {
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("q", imageClientResponse.searchQuery());
        queryParams.add("key", this.tenorConfigProperties.apiKey());
        queryParams.add("searchfilter", this.tenorConfigProperties.searchFilter());
        queryParams.add("contentfilter", this.tenorConfigProperties.contentFilter());
        queryParams.add("mediafilter", this.tenorConfigProperties.mediaFilter());
        queryParams.add("limit", this.tenorConfigProperties.limit());
        return queryParams;
    }

    ;
}
