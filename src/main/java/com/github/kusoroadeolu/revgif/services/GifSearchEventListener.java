package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.gif.BatchGifSearchCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GifSearchEventListener {

    private final SseService sseService;

    @EventListener
    public void streamResults(BatchGifSearchCompletedEvent event){
        final String session = event.session();
        this.sseService.emit(session, event);
    }


}
