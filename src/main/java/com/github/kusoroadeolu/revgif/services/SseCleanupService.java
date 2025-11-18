package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.SseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseCleanupService {

    private final Map<String, SseWrapper> sseEmitters;

    @Value("${sse.duration}")
    private int sseDuration;

    @Async
    @Scheduled(fixedRateString = "${sse.cleanup}")
    public void cleanUpEmitters(){
        final LocalDateTime current = LocalDateTime.now();

        this.sseEmitters.entrySet()
                .forEach((s -> {
                    final String key = s.getKey();
                    final SseWrapper val = s.getValue();
                    if(val.expiresAt().minusMinutes(sseDuration).isAfter(current)){
                        val.sseEmitter().complete();
                        this.sseEmitters.remove(key);
                    }
                }));


    }

}
