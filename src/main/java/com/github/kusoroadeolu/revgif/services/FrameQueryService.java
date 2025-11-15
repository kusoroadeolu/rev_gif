package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;

public interface FrameQueryService {
    void findSimilarMediaFrames(HashWrapper hashWrapper);
}
