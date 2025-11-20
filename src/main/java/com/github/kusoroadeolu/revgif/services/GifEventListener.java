package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.events.GifEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GifEventListener {

    private final SseService sseService;

    @EventListener
    public void streamResults(GifEvent event){
        final String session = event.session();
        this.sseService.emit(session, event);

    }


}
