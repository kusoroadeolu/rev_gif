package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.dtos.gif.BatchDownloadedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.BatchNormalizedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.DownloadedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.NormalizedGif;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GifDownloaderServiceImpl {

    private final WebClient urlDownloadWebClient;
    private final LogMapper logMapper;
    private final GifSimilarityMatcher gifSimilarityMatcher;

    @EventListener
    public void downloadGifsFromUrl(BatchNormalizedGif result){
        log.info("Event Listener triggered ");
        final List<NormalizedGif> results = result.results();
        Flux.fromIterable(results)
                .flatMap(nm ->
                    this.urlDownloadWebClient.get()
                            .uri(nm.url())
                            .retrieve()
                            .bodyToMono(byte[].class)
                            .map(b -> new DownloadedGif(nm, b)
                            )
                )
                .collectList()
                .subscribe(g -> {
                    final BatchDownloadedGif b = new BatchDownloadedGif(g, result.clientResponse());
                    log.info(this.logMapper.log(this.getClass().getSimpleName(), "Successfully downloaded %s gifs".formatted(g.size())));
                    this.gifSimilarityMatcher.extractAndHash(b);
                });
    }

}
