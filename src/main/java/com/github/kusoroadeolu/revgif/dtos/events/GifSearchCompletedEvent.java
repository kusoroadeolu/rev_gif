package com.github.kusoroadeolu.revgif.dtos.events;

public record GifSearchCompletedEvent(
    String tenorUrl,
    String description
) {

}
