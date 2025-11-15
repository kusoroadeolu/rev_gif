package com.github.kusoroadeolu.revgif.dtos.wrappers;

public record FileWrapper(
        byte[] bytes,
        String contentType
) {
}
