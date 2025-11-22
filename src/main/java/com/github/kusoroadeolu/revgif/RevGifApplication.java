package com.github.kusoroadeolu.revgif;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJdbcRepositories
@EnableAsync
@EnableScheduling
@EnableResilientMethods
@ConfigurationPropertiesScan
public class RevGifApplication {
     static void main(String[] args) {
        SpringApplication.run(RevGifApplication.class, args);
    }



}
