package com.github.kusoroadeolu.revgif.dtos.gif;

import java.util.Collection;

public record BatchGifSearchCompletedEvent(
        Collection<GifSearchCompletedEvent> completedEventList,
        String session
) {
}
