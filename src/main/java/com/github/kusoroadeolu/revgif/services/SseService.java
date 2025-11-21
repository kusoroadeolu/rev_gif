package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.wrappers.SseWrapper;
import com.github.kusoroadeolu.revgif.dtos.events.GifEvent;
import com.github.kusoroadeolu.revgif.model.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseService {

    private final Map<String, SseWrapper> sseWrappers;
    private final RedisTemplate<String, Session> sseTemplate;

    @Value("${sse.duration}")
    private int sseDuration;


    public SseEmitter createEmitter(String session){
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.onError(e -> this.sseWrappers.remove(session));
        emitter.onCompletion(() -> this.sseWrappers.remove(session));
        emitter.onTimeout(() -> this.sseWrappers.remove(session));
        this.sseTemplate.opsForValue().set("session:" + session, new Session(session), this.sseDuration, TimeUnit.MINUTES);
        this.sseWrappers.put(session, new SseWrapper(emitter, Executors.newSingleThreadExecutor(Thread.ofVirtual().factory())));
        return emitter;
    }


    public void updateExpectedEvents(String session, int expected){
       final SseWrapper wrapper = this.sseWrappers.get(session);
       wrapper.setExpectedEvents(expected);
    }

    @EventListener
    public void listenForSseExpiry(RedisKeyExpiredEvent<Session> expiredEvent){
        log.info("Redis event triggered");
        final byte[] eventId = expiredEvent.getId();
        final String session = new String(eventId, StandardCharsets.UTF_8);

        if(session.isEmpty()){
            log.info("Empty session found");
           return;
        }

        final SseWrapper wrapper = this.sseWrappers.get(session);
        if(wrapper != null && !wrapper.isCompleted()){
            this.cleanup(session);
        }
        this.sseWrappers.remove(session);
        log.info("Successfully cleaned up emitter. Session: {}", session);
    }

    public void emit(String session, GifEvent event){
        final SseWrapper sseWrapper = this.getWrapper(session);
        final SseEmitter emitter = sseWrapper.sseEmitter();
        sseWrapper.executorService().execute(() -> {
            try{
                log.info("Streaming event to client");
                emitter.send(event);
                sseWrapper.increment();
                sseWrapper.cleanupIfNeeded();
            }catch (IOException ex){
                emitter.completeWithError(ex);
                log.error("An IO ex occurred while streaming...", ex);
                this.cleanup(session);
            }catch (Exception ex){
                emitter.completeWithError(ex);
                log.error("An unexpected ex occurred while streaming...", ex);
                this.cleanup(session);
            }
        });

    }


    public SseWrapper getWrapper(String session){
        SseWrapper wrapper = this.sseWrappers.get(session);
        log.info("Sse wrapper: {}", wrapper);

        if(wrapper == null){
            throw new RuntimeException("Sse wrapper cannot be null");
        }

        return wrapper;
    }

    private void cleanup(String session){
        final SseWrapper wrapper = this.sseWrappers.get(session);
        wrapper.cleanup();
    }

}
