package com.github.kusoroadeolu.revgif.model;

import dev.brachtendorf.jimagehash.hash.Hash;

public record HashWrapper(
        int frameIdx,
        Hash hash
) {
}
