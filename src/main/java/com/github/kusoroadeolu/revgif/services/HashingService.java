package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import lombok.NonNull;

import java.util.List;

public interface HashingService {
    List<HashWrapper> hashFrames(@NonNull List<FrameWrapper> frames);
}
