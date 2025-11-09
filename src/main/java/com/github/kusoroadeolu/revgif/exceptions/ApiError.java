package com.github.kusoroadeolu.revgif.exceptions;

import java.time.LocalDateTime;

public record ApiError(
        int errorCode,
        String message,
        LocalDateTime thrownAt
) {
}
