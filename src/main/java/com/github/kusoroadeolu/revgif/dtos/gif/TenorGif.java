package com.github.kusoroadeolu.revgif.dtos.gif;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public record TenorGif(
        String id,
        @JsonProperty(value = "content_description")
        String contentDescription,
        @JsonProperty(value = "media_formats")
        Map<String, TenorGifMedia> mediaFormats
)
{
}
