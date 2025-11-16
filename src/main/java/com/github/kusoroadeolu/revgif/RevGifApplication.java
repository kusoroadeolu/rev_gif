package com.github.kusoroadeolu.revgif;

import com.github.kusoroadeolu.revgif.model.Frame;
import com.github.kusoroadeolu.revgif.model.Media;
import com.github.kusoroadeolu.revgif.repos.FrameRepository;
import com.github.kusoroadeolu.revgif.repos.MediaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
    public static void main(String[] args) {
        SpringApplication.run(RevGifApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(MediaRepository mediaRepository, FrameRepository frameRepository){
        return args -> {

//            Frame f =
//                    Frame.builder()
//                            .id(null)
//                            .pHash(123456)
//                            .frameIdx(0)
//                            .build();
//
//            Media m = mediaRepository.findById(1L).orElse(null);
//            m.getFrames().add(f);
//            mediaRepository.save(m);

//            Media m =
//                mediaRepository.save(
//                        Media.builder()
//                                .id(1L)
//                                .format("png")
//                                .tenorId("123456")
//                                .tenorUrl("url")
//                                .frames()
//                                .searchKeywords("Key words")
//                                .name("Name")
//                                .createdAt(LocalDateTime.now())
//                                .build()
//                );





        };
    }

}
