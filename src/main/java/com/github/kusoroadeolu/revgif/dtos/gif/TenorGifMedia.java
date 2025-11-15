package com.github.kusoroadeolu.revgif.dtos.gif;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TenorGifMedia(
       String url
) {
}
