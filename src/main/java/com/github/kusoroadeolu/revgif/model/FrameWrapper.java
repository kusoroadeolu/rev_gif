package com.github.kusoroadeolu.revgif.model;

import java.awt.image.BufferedImage;

public record FrameWrapper(
        int frameIdx,
        BufferedImage image
) {
}
