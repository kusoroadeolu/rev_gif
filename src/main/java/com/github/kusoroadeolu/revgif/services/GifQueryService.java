package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.gif.BatchGifSearchCompletedEvent;
import com.github.kusoroadeolu.revgif.dtos.gif.GifSearchCompletedEvent;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;

import java.util.List;
import java.util.Set;

public interface GifQueryService {
    BatchGifSearchCompletedEvent findGifsFromDb(HashWrapper hashWrapper, String session);
}
