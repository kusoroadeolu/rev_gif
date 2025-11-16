package com.github.kusoroadeolu.revgif.dtos;

import dev.brachtendorf.jimagehash.hash.Hash;

public record ImageClientResponse(
        String searchQuery,
        String format,
        int frameIdx,
        Hash hash
) {
}
