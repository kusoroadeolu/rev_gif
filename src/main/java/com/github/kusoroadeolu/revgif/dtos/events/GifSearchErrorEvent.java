package com.github.kusoroadeolu.revgif.dtos.events;

import java.time.LocalDateTime;

public record GifSearchErrorEvent(
        String errorMessage,
        EventErrorType errorType,
        String session,
        LocalDateTime occurredAt
) implements GifEvent{

}
