package com.github.kusoroadeolu.revgif.dtos;

import org.jspecify.annotations.NonNull;

import java.time.Instant;

public record LogDump(
        String loggedAt,
        String loggedFrom,
        String logMessage
) {

    @Override
    @NonNull
    public String toString(){
        return """
                thrownAt: %s
                thrownFrom: %s
                errorMessage: %s
                """.formatted(loggedAt, loggedFrom, logMessage);
    }
}
