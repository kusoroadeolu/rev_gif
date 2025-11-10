package com.github.kusoroadeolu.revgif.model.wrappers;

import java.awt.image.BufferedImage;

public record FrameWrapper(
        int frameIdx,
        BufferedImage image
) {
}
