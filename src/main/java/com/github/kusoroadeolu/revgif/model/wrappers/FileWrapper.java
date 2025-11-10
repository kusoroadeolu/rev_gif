package com.github.kusoroadeolu.revgif.model.wrappers;

public record FileWrapper(
        byte[] bytes,
        String contentType
) {
}
