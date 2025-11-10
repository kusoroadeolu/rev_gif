package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.model.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.model.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.services.HashingService;
import dev.brachtendorf.jimagehash.hash.Hash;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Component
@Slf4j
@RequiredArgsConstructor
public class HashingServiceImpl implements HashingService {

    private final HashingAlgorithm hasher;
    private final ExecutorService workStealingExecutorService;

    @Override
    public CompletableFuture<List<HashWrapper>> hashFrames(@NonNull List<FrameWrapper> frames){
        return CompletableFuture.supplyAsync(() -> {
            final List<HashWrapper> hws = new ArrayList<>();
            FrameWrapper fw;
            for (FrameWrapper frame : frames) {
                fw = frame;
                final Hash hash = this.hasher.hash(fw.image());
                final HashWrapper w = new HashWrapper(fw.frameIdx(), hash);
                hws.add(w);
                log.info("Added hash at idx: {}, hash: {}", w.frameIdx(), w.hash().getHashValue());
            }

            return hws;
        }, this.workStealingExecutorService);
    }

}
