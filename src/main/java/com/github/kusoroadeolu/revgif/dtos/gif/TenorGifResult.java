package com.github.kusoroadeolu.revgif.dtos.gif;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record TenorGifResult(
        String id,
        String title,
        @JsonProperty(value = "media_formats")
        Map<String, TenorGifMedia> mediaFormats
)
{
}
