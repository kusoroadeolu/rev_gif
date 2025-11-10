package com.github.kusoroadeolu.revgif;

import com.github.kusoroadeolu.revgif.model.FrameHash;
import com.github.kusoroadeolu.revgif.model.Gif;
import com.github.kusoroadeolu.revgif.repos.GifRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@SpringBootApplication
@EnableJdbcRepositories
public class RevGifApplication {
    public static void main(String[] args) {
        SpringApplication.run(RevGifApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(GifRepository repository){
        return args -> {

        };
    }

}
