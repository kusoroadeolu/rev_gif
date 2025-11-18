package com.github.kusoroadeolu.revgif;

import com.github.kusoroadeolu.revgif.services.SseService;
import com.github.kusoroadeolu.revgif.services.UploadOrchestrator;
import com.github.kusoroadeolu.revgif.services.impl.CookieUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class GifController {

    private final UploadOrchestrator orchestrator;
    private final CookieUtils cookieUtils;
    private final SseService sseService;

    @GetMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<@NonNull SseEmitter> upload(@RequestParam("file") MultipartFile file, HttpServletRequest request, HttpServletResponse response) throws IOException {
        final Cookie cookie = this.cookieUtils.getCookie(request);
        this.cookieUtils.addCookie(request, response, cookie);
        final String session = cookie.getValue();
        final SseEmitter emitter = this.sseService.getWrapper(session)
                .sseEmitter();
        orchestrator.orchestrate(file.getBytes(), session);
        return new ResponseEntity<>(emitter, HttpStatus.OK);
    }



}
