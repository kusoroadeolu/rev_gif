package com.github.kusoroadeolu.revgif.config;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@RequiredArgsConstructor
public class MiscConfig {

    private final AppConfigProperties properties;

    @Bean
    public TaskExecutor taskExecutor(){
        return new VirtualThreadTaskExecutor();
    }



    @Bean
    public Map<UUID, SseEmitter> emitters(){
        return new ConcurrentHashMap<>();
    }

    @Bean
    public HashingAlgorithm hasher(){
        return new PerceptiveHash(this.properties.getBitResolution());
    }

}
