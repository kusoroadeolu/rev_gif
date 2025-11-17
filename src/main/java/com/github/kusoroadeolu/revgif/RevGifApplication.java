package com.github.kusoroadeolu.revgif;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableResilientMethods
@EnableJdbcRepositories
@EnableConfigurationProperties
@EnableAsync
@ConfigurationPropertiesScan
public class RevGifApplication {
     static void main(String[] args) {
        SpringApplication.run(RevGifApplication.class, args);
    }

}
