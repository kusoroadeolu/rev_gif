package com.github.kusoroadeolu.revgif.dtos.events;

import java.util.List;

public record BatchGifSearchCompletedEvent (
        List<? extends GifSearchCompletedEvent> completedEventList,
        String session
)  implements GifEvent {
}
