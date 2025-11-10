package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.model.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.model.wrappers.HashWrapper;
import lombok.NonNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface HashingService {
    CompletableFuture<List<HashWrapper>> hashFrames(@NonNull List<FrameWrapper> frames);
}
