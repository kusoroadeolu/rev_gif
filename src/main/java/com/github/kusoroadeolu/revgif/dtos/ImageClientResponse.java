package com.github.kusoroadeolu.revgif.dtos;

public record ImageClientResponse(
        String searchQuery,
        String format,
        int frameIdx,
        Long hash
) {
}
