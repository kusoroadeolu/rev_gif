package com.github.kusoroadeolu.revgif.dtos.wrappers;

import dev.brachtendorf.jimagehash.hash.Hash;

public record HashWrapper(
        FrameWrapper frameWrapper,
        Hash hash
) {
}
