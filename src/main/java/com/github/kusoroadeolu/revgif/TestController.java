package com.github.kusoroadeolu.revgif;

import com.github.kusoroadeolu.revgif.services.UploadOrchestrator;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final UploadOrchestrator orchestrator;


    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<@NonNull Void> upload(@RequestParam("file") MultipartFile file) throws IOException {
        log.info("Successfully hit endpoint: upload");
        orchestrator.orchestrate(file.getBytes());
        //orchestrator.testDb();
        return ResponseEntity.ok().build();
    }



}
