package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.gif.BatchDownloadedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.BatchGifSearchCompletedEvent;
import com.github.kusoroadeolu.revgif.dtos.gif.DownloadedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.HashedGif;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.exceptions.GifMatchingException;
import com.github.kusoroadeolu.revgif.mappers.FrameMapper;
import com.github.kusoroadeolu.revgif.mappers.GifMapper;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.model.Frame;
import com.github.kusoroadeolu.revgif.model.Gif;
import dev.brachtendorf.jimagehash.hash.Hash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class GifSimilarityMatcher {

    private final FrameExtractorService frameExtractorService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final HashingService hashingService;
    private final AppConfigProperties appConfigProperties;
    private final GifCommandService gifCommandService;
    private final FrameMapper frameMapper;
    private final GifMapper gifMapper;
    private final LogMapper logMapper;
    private final TaskExecutor taskExecutor;
    private final static String CLASS_NAME = GifSimilarityMatcher.class.getSimpleName();

    public void extractAndHash(BatchDownloadedGif bdf, String session){
        final Hash hash = bdf.clientResponse().hash();
        final String format = bdf.clientResponse().format();
        final String query = bdf.clientResponse().searchQuery();
        final List<HashedGif> hashedGifs = new ArrayList<>();

        for (final DownloadedGif g : bdf.downloadedGifs()){
            final byte[] b = g.bytes();
            final List<FrameWrapper> fws = this.frameExtractorService.extractFrames(b, format);
            final List<HashWrapper> hws = this.hashingService.hashFrames(fws);
            hashedGifs.add(new HashedGif(g.normalizedGif(), hws));
        }


        final List<Gif> similarGifs = new CopyOnWriteArrayList<>();
        final List<CompletableFuture> futures = new ArrayList<>();
        for (final HashedGif hf : hashedGifs){
            futures.add(CompletableFuture.runAsync(() -> this.compareHashAgainstFrames(hf, hash, query, format, similarGifs), this.taskExecutor));
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)) //Wait for each future to complete before saving to the db
                .thenRun(() -> this.applicationEventPublisher.publishEvent(new BatchGifSearchCompletedEvent(this.gifMapper.toSearchCompletedEvent(similarGifs), session)))
                .thenRun(() -> this.gifCommandService.batchSave(similarGifs))
                .exceptionally(e -> {
                    log.error(this.logMapper.log(CLASS_NAME, "An unexpected error occurred: "), e);
                    throw new GifMatchingException(e);
                });
        log.info("Final size: {}", similarGifs.size());

    }


    private void compareHashAgainstFrames(HashedGif hf, Hash hash, String query, String format, List<Gif> gifs){
        final List<HashWrapper> hw = hf.hashWrappers();
        for (HashWrapper h : hw){
            final double hd = h.hash().normalizedHammingDistance(hash);
            if(hd <= this.appConfigProperties.nmHammingThreshold()){
                final Set<Frame> frames = this.frameMapper.toFrame(hw);
                final Gif g = this.gifMapper.toGif(hf, query, format, frames);
                this.logMapper.log(CLASS_NAME, "Found similar gif. Hamming dist: %s".formatted(hd));
                gifs.add(g);
                this.logMapper.log(CLASS_NAME, "Current list size: %s".formatted(gifs.size()));
                break;
            }
        }
    }







}
