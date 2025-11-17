package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.dtos.gif.BatchDownloadedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.BatchNormalizedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.DownloadedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.NormalizedGif;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.services.GifSimilarityMatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GifDownloadServiceImpl implements com.github.kusoroadeolu.revgif.services.GifDownloadService {

    private final WebClient urlDownloadWebClient;
    private final LogMapper logMapper;
    private final GifSimilarityMatcher gifSimilarityMatcherImpl;
    private final static String CLASS_NAME = GifDownloadServiceImpl.class.getSimpleName();

    @EventListener
    @Override
    public void downloadGifsFromUrl(@NonNull BatchNormalizedGif result){
        log.info("Event Listener triggered ");
        final List<NormalizedGif> results = result.results();
        Flux.fromIterable(results)
                .flatMap(nm ->
                        this.urlDownloadWebClient.get()
                                .uri(nm.url())
                                .retrieve()
                                .bodyToMono(byte[].class)
                                .map(b -> new DownloadedGif(nm, b))
                                .doOnError(e -> log.error(this.logMapper.log(CLASS_NAME, "An error occurred while trying downloading a file from url: %s".formatted(nm.url()))))
                                .onErrorResume(e -> Mono.empty())

                )
                .collectList()
                .doOnError(e -> log.error(this.logMapper.log(CLASS_NAME, "An error occurred while batch downloading files"), e))
                .onErrorResume(e -> Mono.empty())
                .subscribe(g -> {
                    final BatchDownloadedGif b = new BatchDownloadedGif(g, result.clientResponse());
                    log.info(this.logMapper.log(CLASS_NAME, "Successfully downloaded %s gifs".formatted(g.size())));
                    this.gifSimilarityMatcherImpl.extractAndHash(b);
                });
    }

}
