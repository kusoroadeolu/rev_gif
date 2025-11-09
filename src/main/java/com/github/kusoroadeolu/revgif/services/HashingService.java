package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.model.FrameWrapper;
import com.github.kusoroadeolu.revgif.model.HashWrapper;
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
public class HashingService {

    private final HashingAlgorithm hasher;
    private final ExecutorService pExecutorService;

    public CompletableFuture<List<HashWrapper>> hashFrames(@NonNull List<FrameWrapper> frames){
        return CompletableFuture.supplyAsync(() -> {
            final List<HashWrapper> hws = new ArrayList<>();
            FrameWrapper fw;
            for (int i = 0; i < frames.size(); i++){
                fw = frames.get(i);
                hws.add(new HashWrapper(fw.frameIdx(), this.hasher.hash(fw.image())));
            }

            return hws;
        }, this.pExecutorService);
    }

}
