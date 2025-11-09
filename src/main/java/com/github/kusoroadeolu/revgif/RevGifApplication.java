package com.github.kusoroadeolu.revgif;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

@SpringBootApplication
@EnableJdbcRepositories
public class RevGifApplication {
    public static void main(String[] args) {
        SpringApplication.run(RevGifApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner commandLineRunner(GifRepository repository){
//        return args -> {
//            Gif gif = Gif.builder()
//                    .gifUrl("dummyurl")
//                    .searchKeywords("keywords")
//                    .tenorId("dummyid")
//                    .pHash(123456);
//
//            repository.save(gif);
//        };
//    }

}
