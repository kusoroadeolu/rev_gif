package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.dtos.gif.BatchDownloadedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.DownloadedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.HashedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.NormalizedGif;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.mappers.FrameMapper;
import com.github.kusoroadeolu.revgif.mappers.GifMapper;
import com.github.kusoroadeolu.revgif.model.Frame;
import com.github.kusoroadeolu.revgif.model.Gif;
import com.github.kusoroadeolu.revgif.services.FrameExtractorService;
import com.github.kusoroadeolu.revgif.services.GifCommandService;
import com.github.kusoroadeolu.revgif.services.HashingService;
import dev.brachtendorf.jimagehash.hash.Hash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GifSimilarityMatcher {

    private final FrameExtractorService frameExtractorService;
    private final HashingService hashingService;
    private final GifCommandService gifCommandService;
    private final FrameMapper frameMapper;
    private final GifMapper gifMapper;
    private final TaskExecutor taskExecutor;

    public void extractAndHash(BatchDownloadedGif bdf){
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

        final List<Gif> gifsToSave = new CopyOnWriteArrayList<>();
        final List<CompletableFuture> futures = new ArrayList<>();
        for (final HashedGif hf : hashedGifs){
            futures.add(CompletableFuture.runAsync(() -> this.compareHashAgainstFrames(hf, hash, query, format, gifsToSave), this.taskExecutor));
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)) //Wait for each future to complete before saving to the db
                .thenRunAsync(() -> this.gifCommandService.batchSave(gifsToSave), this.taskExecutor);

    }

    private void compareHashAgainstFrames(HashedGif hf, Hash hash, String query, String format, List<Gif> gifs){
        final List<HashWrapper> hw = hf.hashWrappers();
        for (HashWrapper h : hw){
            final double hd = h.hash().normalizedHammingDistance(hash);
            if(hd < 0.285){
                final Set<Frame> frames = this.frameMapper.toFrame(hw);
                final Gif g = this.gifMapper.toGif(hf, query, format, frames);
                gifs.add(g);
                break;
            }
        }
    }







}
