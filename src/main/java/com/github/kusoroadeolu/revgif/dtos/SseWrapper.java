package com.github.kusoroadeolu.revgif.dtos;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;

public record SseWrapper(
        SseEmitter sseEmitter,
        ExecutorService executorService,
        LocalDateTime expiresAt
) {
}
