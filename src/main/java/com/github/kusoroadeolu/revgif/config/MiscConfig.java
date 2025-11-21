package com.github.kusoroadeolu.revgif.config;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.wrappers.SseWrapper;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@RequiredArgsConstructor
public class MiscConfig {

    private final AppConfigProperties properties;

    @Bean
    public TaskExecutor taskExecutor(){
        return new VirtualThreadTaskExecutor();
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource){
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    public Map<String, SseWrapper> sseWrappers(){
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Tika tika()  {
        return new Tika();
    }

    @Bean
    public HashingAlgorithm hasher(){
        return new PerceptiveHash(this.properties.bitResolution());
    }

}
