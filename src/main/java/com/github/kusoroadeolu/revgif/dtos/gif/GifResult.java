package com.github.kusoroadeolu.revgif.dtos.gif;

import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;

import java.util.List;

public record GifResult(
        List<NormalizedGifResult> results,
        ImageClientResponse clientResponse
) {
}
