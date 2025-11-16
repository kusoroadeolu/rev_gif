package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.dtos.gif.BatchDownloadedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.DownloadedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.HashedGif;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.services.FrameExtractorService;
import com.github.kusoroadeolu.revgif.services.HashingService;
import dev.brachtendorf.jimagehash.hash.Hash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class GifSimilarityMatcher {

    private final FrameExtractorService frameExtractorService;
    private final HashingService hashingService;

    public void extractAndHash(BatchDownloadedGif bdf){
        final Hash hash = bdf.clientResponse().hash();
        final String format = bdf.clientResponse().format();
        final String query = bdf.clientResponse().searchQuery();
        final List<HashedGif> hashedGifs = new ArrayList<>();

        for (DownloadedGif g : bdf.downloadedGifs()){
            final byte[] b = g.bytes();
            final List<FrameWrapper> fws = this.frameExtractorService.extractFrames(b, format);
            final List<HashWrapper> hws = this.hashingService.hashFrames(fws);
            hashedGifs.add(new HashedGif(g.normalizedGif(), hws));
        }

        List<QuickTest> qs = new ArrayList<>();

        for (HashedGif hf : hashedGifs){
            final List<HashWrapper> hw = hf.hashWrappers();
            for (HashWrapper h : hw){
                final double hd = h.hash().normalizedHammingDistance(hash);
                if(hd < 0.3){
                    qs.add(new QuickTest(hd, hf.normalizedGif().url()));
                    //Todo Add to list, after the loop is complete we publish an event to the SSE emitter and find a way to save lol
                    break;
                }

            }
        }

        for (QuickTest qt : qs){
            log.info("Similar gif found. Hamming dist: {}, Url: {}", qt.hammingDist(), qt.url());
        }
    }

    record QuickTest(double hammingDist, String url){

    }

}
