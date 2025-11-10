package com.github.kusoroadeolu.revgif.configprops;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@ConfigurationProperties("spring.application")
@Setter
@Getter
public class AppConfigProperties {
    private Set<String> allowedFileFormats;
    private int bitResolution;
    private int expectedFrames;
}
