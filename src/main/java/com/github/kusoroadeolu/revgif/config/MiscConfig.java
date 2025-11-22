package com.github.kusoroadeolu.revgif.config;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.wrappers.SseWrapper;
import dev.brachtendorf.jimagehash.hashAlgorithms.HashingAlgorithm;
import dev.brachtendorf.jimagehash.hashAlgorithms.PerceptiveHash;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@RequiredArgsConstructor
public class MiscConfig {

    private final AppConfigProperties properties;
    private final JdbcTemplate jdbcTemplate;

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


    @Bean
    public CommandLineRunner run(){
        return _ ->
            this.jdbcTemplate.execute(
                    """                        
                            CREATE TABLE IF NOT EXISTS gifs (
                            id BIGSERIAL PRIMARY KEY,
                            mime_type VARCHAR(20),
                            description VARCHAR(300) NOT NULL,
                            tenor_url VARCHAR(300) UNIQUE NOT NULL,
                            tenor_id VARCHAR(200) NOT NULL,
                            search_query VARCHAR(20) NOT NULL,
                            created_at TIMESTAMP DEFAULT NOW(),
                            updated_at TIMESTAMP DEFAULT NOW()
                        );
                        
                        CREATE TABLE IF NOT EXISTS frames(
                            id BIGSERIAL PRIMARY KEY,
                            p_hash BIGINT NOT NULL,
                            frame_idx INT NOT NULL,
                            nm_hamming_dist DOUBLE PRECISION NOT NULL,
                            gifs BIGINT NOT NULL,
                            FOREIGN KEY (gifs) REFERENCES gifs(id) ON DELETE CASCADE
                        );
                        
                        CREATE INDEX IF NOT EXISTS idx_p_hashes ON frames(p_hash);
                        CREATE INDEX IF NOT EXISTS idx_gifs ON frames(gifs);
                        """
            );
    }
}
