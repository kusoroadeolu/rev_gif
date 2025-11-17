package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.gif.GifSearchCompletedEvent;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;

import java.util.Set;

public interface GifQueryService {
    Set<GifSearchCompletedEvent> findGifsFromDb(HashWrapper hashWrapper);
}
