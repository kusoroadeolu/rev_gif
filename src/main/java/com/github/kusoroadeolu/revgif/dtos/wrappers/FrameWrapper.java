package com.github.kusoroadeolu.revgif.dtos.wrappers;

import java.awt.image.BufferedImage;

public record FrameWrapper(
        int frameIdx,
        BufferedImage image,
        String format
) {
}
