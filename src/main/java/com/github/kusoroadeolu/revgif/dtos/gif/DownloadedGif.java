package com.github.kusoroadeolu.revgif.dtos.gif;

public record DownloadedGif(
        NormalizedGif normalizedGif,
        byte[] bytes
) {
}
