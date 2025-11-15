package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.wrappers.FileWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;

import java.util.List;

public interface FrameExtractorService {
    List<FrameWrapper> extractFrames(FileWrapper fileWrapper);
}
