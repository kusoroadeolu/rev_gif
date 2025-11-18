package com.github.kusoroadeolu.revgif;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJdbcRepositories
@EnableConfigurationProperties
@EnableAsync
@EnableScheduling
@ConfigurationPropertiesScan
public class RevGifApplication {
     static void main(String[] args) {
        SpringApplication.run(RevGifApplication.class, args);
    }

}
