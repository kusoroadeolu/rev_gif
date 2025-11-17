package com.github.kusoroadeolu.revgif.dtos.gif;

public record GifSearchCompletedEvent(
    String tenorUrl,
    String description,
    String mimeType
) {

}
