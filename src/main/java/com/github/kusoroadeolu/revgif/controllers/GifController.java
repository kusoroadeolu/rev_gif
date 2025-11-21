package com.github.kusoroadeolu.revgif.controllers;

import com.github.kusoroadeolu.revgif.services.SseService;
import com.github.kusoroadeolu.revgif.services.UploadOrchestrator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GifController {

    private final UploadOrchestrator orchestrator;
    private final SseService sseService;

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<@NonNull SseEmitter> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String session = UUID.randomUUID().toString();
        final SseEmitter emitter = this.sseService.createEmitter(session);
        orchestrator.orchestrate(file.getBytes(), session);
        return new ResponseEntity<>(emitter, HttpStatus.OK);
    }



}
