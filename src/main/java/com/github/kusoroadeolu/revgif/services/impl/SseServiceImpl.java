package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.dtos.SseWrapper;
import com.github.kusoroadeolu.revgif.services.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SseServiceImpl implements SseService {

    private final Map<String, SseWrapper> sseWrappers;

    private SseEmitter createEmitter(String session){
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.onError(e -> sseWrappers.remove(session));
        emitter.onCompletion(() -> sseWrappers.remove(session));
        emitter.onTimeout(() -> sseWrappers.remove(session));
        return emitter;
    }

    @Override
    public void emit(String session, Object val){
        final SseWrapper sseWrapper = this.getWrapper(session);
        final SseEmitter emitter = sseWrapper.sseEmitter();
        try(final ExecutorService e = sseWrapper.executorService()) {
            e.execute(() -> {
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
    }

    @Override
    public void remove(String session){
        final SseWrapper sseWrapper = this.sseWrappers.get(session);
        final SseEmitter emitter = sseWrapper.sseEmitter();
        emitter.complete();
        this.sseWrappers.remove(session);
    }

    @Override
    public SseWrapper getWrapper(String session){
        SseWrapper wrapper = this.sseWrappers.get(session);

        if(wrapper == null){
            final SseEmitter emitter = this.createEmitter(session);
            wrapper = new SseWrapper(emitter, Executors.newVirtualThreadPerTaskExecutor());
            this.sseWrappers.put(session, wrapper);
        }

        return wrapper;
    }

}
