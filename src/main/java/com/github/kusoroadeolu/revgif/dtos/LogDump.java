package com.github.kusoroadeolu.revgif.dtos;

import org.jspecify.annotations.NonNull;

public record LogDump(
        String loggedAt,
        String loggedFrom,
        String logMessage
) {

    @Override
    @NonNull
    public String toString() {
        return "loggedAt=%s, loggedFrom=%s, logMessage=%s"
                .formatted(
                        loggedAt,
                        loggedFrom,
                        sanitize(logMessage)
                );
    }

    private static String sanitize(String msg) {
        if (msg == null) return "null";
        return msg.replace("\n", "\\n").replace("\r", "\\r");
    }

}
