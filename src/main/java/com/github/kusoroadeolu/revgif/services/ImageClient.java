package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;

public interface ImageClient {
    ImageClientResponse getFrameDescription(HashWrapper wrapper);
}
