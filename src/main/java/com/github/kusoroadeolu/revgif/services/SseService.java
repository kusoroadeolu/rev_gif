package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.SseWrapper;
import com.github.kusoroadeolu.revgif.dtos.gif.BatchGifSearchCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseService {

    private final Map<String, SseWrapper> sseWrappers;

    @Value("${sse.duration}")
    private int sseDuration;

    private SseEmitter createEmitter(String session){
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.onError(e -> sseWrappers.remove(session));
        emitter.onCompletion(() -> sseWrappers.remove(session));
        emitter.onTimeout(() -> sseWrappers.remove(session));
        return emitter;
    }


    public void emit(String session, BatchGifSearchCompletedEvent val){
        final SseWrapper sseWrapper = this.getWrapper(session);
        final SseEmitter emitter = sseWrapper.sseEmitter();
        sseWrapper.executorService().execute(() -> {
            try{
                log.info("Streaming event to client");
                emitter.send(val);
            }catch (IOException ex){
                emitter.completeWithError(ex);
                log.error("An IO ex occurred while streaming...");
                sseWrappers.remove(session);
            }
        });
    }


    public void remove(String session){
        final SseWrapper sseWrapper = this.sseWrappers.get(session);
        final SseEmitter emitter = sseWrapper.sseEmitter();
        emitter.complete();
        this.sseWrappers.remove(session);
    }

    public SseWrapper getWrapper(String session){
        SseWrapper wrapper = this.sseWrappers.get(session);

        if(wrapper == null){
            final SseEmitter emitter = this.createEmitter(session);
            wrapper = new SseWrapper(emitter, Executors.newVirtualThreadPerTaskExecutor(), LocalDateTime.now().plusMinutes(this.sseDuration));
            this.sseWrappers.put(session, wrapper);
        }

        return wrapper;
    }

}
