package com.github.kusoroadeolu.revgif.dtos.gif;

import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;

import java.util.List;

public record HashedGif(
        NormalizedGif normalizedGif,
        List<HashWrapper> hashWrappers
) {
}
