package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.events.BatchGifSearchCompletedEvent;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;

public interface GifQueryService {
    BatchGifSearchCompletedEvent findGifsFromDb(HashWrapper hashWrapper, String session);
}
