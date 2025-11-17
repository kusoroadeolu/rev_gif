package com.github.kusoroadeolu.revgif.configprops;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;


@ConfigurationProperties("spring.application")
public record AppConfigProperties(
        Set<String> allowedFileFormats,
        int bitResolution,
        int expectedFrames,
        int hammingThreshold,
        double nmHammingThreshold
) {

}
