package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.SseWrapper;
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

    @Value("{sse.keyspace}")
    private String prefix;

    @Value("${sse.duration}")
    private int sseDuration;


    private SseEmitter createEmitter(String session){
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.onError(e -> sseWrappers.remove(session));
        emitter.onCompletion(() -> sseWrappers.remove(session));
        emitter.onTimeout(() -> sseWrappers.remove(session));
        this.sseTemplate.opsForValue().set(prefix + ":" + session, new Session(session), this.sseDuration, TimeUnit.MINUTES);
        return emitter;
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
        wrapper.sseEmitter().complete();
        this.cleanup(session);
        log.info("Successfully cleaned up emitter. Session: {}", session);
    }


    public void emit(String session, GifEvent event){
        final SseWrapper sseWrapper = this.getWrapper(session);
        final SseEmitter emitter = sseWrapper.sseEmitter();
        log.info("Emitter: {}", emitter);
        sseWrapper.executorService().execute(() -> {
            try{
                log.info("Streaming event to client");
                emitter.send(event);
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

        if(wrapper == null){
            final SseEmitter emitter = this.createEmitter(session);
            wrapper = new SseWrapper(emitter, Executors.newVirtualThreadPerTaskExecutor());
            this.sseWrappers.put(session, wrapper);
        }

        return wrapper;
    }

    private void cleanup(String session){
        final SseWrapper wrapper = this.sseWrappers.get(session);
        wrapper.executorService().shutdown();
        wrapper.executorService().close();
        this.sseWrappers.remove(session);
    }

}
