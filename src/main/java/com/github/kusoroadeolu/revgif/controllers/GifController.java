package com.github.kusoroadeolu.revgif.controllers;

import com.github.kusoroadeolu.revgif.exceptions.FileReadException;
import com.github.kusoroadeolu.revgif.exceptions.UnsupportedFileFormatException;
import com.github.kusoroadeolu.revgif.services.SseService;
import com.github.kusoroadeolu.revgif.services.UploadOrchestrator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Image/Gif Upload", description = "Upload a gif/image")
@ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image/gif uploaded successfully", content = @Content(schema = @Schema(implementation = SseEmitter.class))),
        @ApiResponse(responseCode = "400", description = "Uploaded file with unsupported mime type", content = @Content(schema = @Schema(implementation = UnsupportedFileFormatException.class))),
        @ApiResponse(responseCode = "500", description = "Failed to read uploaded image/gif", content = @Content(schema = @Schema(implementation = FileReadException.class)))
})
public class GifController {

    private final UploadOrchestrator orchestrator;
    private final SseService sseService;

    @PostMapping(path = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @Operation(description = "Accepts an image, validates it, extracts frames from it, computes the hash of those frames and fetches similar images to those frames")
    public ResponseEntity<@NonNull SseEmitter> upload(@Parameter(description = "File to be uploaded", required = true) @RequestParam("file") MultipartFile file) throws IOException {
        final String requestId = UUID.randomUUID().toString(); //This is ref as session across the code base, but it's actually the request ID. I cant just bother myself refactoring it
        final SseEmitter emitter = this.sseService.createEmitter(requestId);
        orchestrator.orchestrate(file.getBytes(), requestId);
        return new ResponseEntity<>(emitter, HttpStatus.OK);
    }



}
