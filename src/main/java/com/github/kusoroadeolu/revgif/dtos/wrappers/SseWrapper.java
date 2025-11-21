package com.github.kusoroadeolu.revgif.dtos.wrappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SseWrapper {
    private final SseEmitter sseEmitter;
    private final ExecutorService executorService;
    private int expectedEvents;
    private int eventsReceived;
    private State state;

    public SseWrapper(SseEmitter sseEmitter, ExecutorService executorService){
        this.sseEmitter = sseEmitter;
        this.executorService = executorService;
        this.eventsReceived = 0;
        this.state = State.STREAMING;
    }


    public void setExpectedEvents(int expectedEvents){
        this.expectedEvents = expectedEvents;
    }

    public synchronized void increment(){
        this.eventsReceived++;
    }

    public synchronized void cleanupIfNeeded(){
        log.info("Cleanup check. Events received: {}", this.eventsReceived());
        if(this.eventsReceived() >= this.expectedEvents){
            this.state = State.COMPLETING;
            if(this.sseEmitter != null){
                this.sseEmitter.complete();
            }

            if(this.executorService != null){
                this.executorService.shutdown();
                this.executorService.close();
            }

            this.state = State.COMPLETED;
            IO.println("Successfully cleaned up the sse emitter. Events expected: %s, Events received: %s".formatted(this.expectedEvents, this.eventsReceived()));
        }
    }

    public int expectedEvents() {
        return this.expectedEvents;
    }

    public int eventsReceived() {
        return this.eventsReceived;
    }

    public ExecutorService executorService() {
        return this.executorService;
    }

    public boolean isCompleted() {
        return this.state == State.COMPLETED;
    }

    public SseEmitter sseEmitter() {
        return this.sseEmitter;
    }

    private enum State{
        STREAMING,
        COMPLETING,
        COMPLETED
    }
}
