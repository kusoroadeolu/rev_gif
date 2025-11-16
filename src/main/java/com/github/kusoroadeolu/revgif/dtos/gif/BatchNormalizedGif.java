package com.github.kusoroadeolu.revgif.dtos.gif;

import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;

import java.util.List;

public record BatchNormalizedGif(
        List<NormalizedGif> results,
        ImageClientResponse clientResponse
) {
    @Override
    public String toString() {
        return "BatchNormalizedGif{" +
                "gifs=" + (results != null
                ? results.stream()
                .map(g -> "{id=%s, url=%s}".formatted(g.id(), g.url()))
                .toList()
                : "null") +
                ", clientResponse=" + clientResponse +
                '}';
    }

}
