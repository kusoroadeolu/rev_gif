package com.github.kusoroadeolu.revgif.dtos.events;

import java.util.Collection;

public record BatchGifSearchCompletedEvent (
        Collection<? extends GifSearchCompletedEvent> completedEventList,
        String session
)  implements GifEvent {
}
