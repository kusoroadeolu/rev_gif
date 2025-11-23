package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.events.GifEvent;
import com.github.kusoroadeolu.revgif.dtos.wrappers.SseWrapper;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.model.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseService {

    private final Map<String, SseWrapper> sseWrappers;
    private final RedisTemplate<String, Session> sseTemplate;
    private final LogMapper logMapper;
    private static final String CLASS_NAME = SseService.class.getSimpleName();

    @Value("${sse.duration}")
    private int sseDuration;


    public SseEmitter createEmitter(@NonNull String session){
        final SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.onError(e -> this.sseWrappers.remove(session));
        emitter.onCompletion(() -> this.sseWrappers.remove(session));
        emitter.onTimeout(() -> this.sseWrappers.remove(session));
        this.sseTemplate.opsForValue().set("session:" + session, new Session(session), this.sseDuration, TimeUnit.MINUTES);
        this.sseWrappers.put(session, new SseWrapper(emitter, Executors.newSingleThreadExecutor(Thread.ofVirtual().factory())));
        return emitter;
    }


    public void updateExpectedEvents(@NonNull String session, int expected){
       final SseWrapper wrapper = this.sseWrappers.get(session);
       wrapper.setExpectedEvents(expected);
    }

    public int getExpectedEvents(@NonNull String session){
        return this.sseWrappers.get(session).expectedEvents();
    }

    @EventListener
    public void listenForSseExpiry(RedisKeyExpiredEvent<Session> expiredEvent){
        log.info(this.logMapper.log(CLASS_NAME, "Redis event triggered"));
        final byte[] eventId = expiredEvent.getId();
        final String session = new String(eventId, StandardCharsets.UTF_8);

        if(session.isEmpty()){
            log.info(this.logMapper.log(CLASS_NAME,  "Empty session found"));
           return;
        }

        final SseWrapper wrapper = this.sseWrappers.get(session);
        if(wrapper != null && !wrapper.isCompleted()){
            wrapper.cleanup();
        }

        this.sseWrappers.remove(session);
        log.info(this.logMapper.log(CLASS_NAME, "Successfully cleaned up emitter. Session: %s".formatted(session)));
    }

    public void emit(@NonNull String session, GifEvent event){
        final SseWrapper sseWrapper = this.sseWrappers.get(session);
        if (sseWrapper == null) return;

        final SseEmitter emitter = sseWrapper.sseEmitter();
        final ExecutorService executor = sseWrapper.executorService();
        if(emitter != null && executor != null){
            executor.execute(() -> {
                try{
                    log.info("Streaming event to client");
                    emitter.send(event);
                    sseWrapper.increment();
                    sseWrapper.cleanupIfNeeded();
                }catch (IOException ex){
                    emitter.completeWithError(ex);
                    log.error("An IO ex occurred while streaming...", ex);
                    sseWrapper.cleanup();
                }catch (Exception ex){
                    emitter.completeWithError(ex);
                    log.error("An unexpected ex occurred while streaming...", ex);
                    sseWrapper.cleanup();
                }
            });
        }

    }




}
