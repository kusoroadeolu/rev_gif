package com.github.kusoroadeolu.revgif.dtos.events;

public sealed interface GifEvent permits BatchGifSearchCompletedEvent, GifSearchErrorEvent{
    String session();
}
