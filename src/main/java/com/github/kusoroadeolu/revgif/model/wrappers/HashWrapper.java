package com.github.kusoroadeolu.revgif.model.wrappers;

import dev.brachtendorf.jimagehash.hash.Hash;

public record HashWrapper(
        int frameIdx,
        Hash hash
) {
}
