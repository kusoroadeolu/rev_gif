package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.model.wrappers.FileWrapper;
import com.github.kusoroadeolu.revgif.model.wrappers.FrameWrapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface FrameExtractorService {
    CompletableFuture<List<FrameWrapper>> extractFrames(FileWrapper fileWrapper);
}
