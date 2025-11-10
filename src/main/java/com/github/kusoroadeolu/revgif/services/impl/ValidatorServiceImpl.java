package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.exceptions.FileReadException;
import com.github.kusoroadeolu.revgif.exceptions.UnsupportedFileFormatException;
import com.github.kusoroadeolu.revgif.model.wrappers.FileWrapper;
import com.github.kusoroadeolu.revgif.services.ValidatorService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidatorServiceImpl implements ValidatorService {

    private final AppConfigProperties configProperties;
    private final ExecutorService vExecutorService;

    @Override
    public CompletableFuture<FileWrapper> validateFile(@NonNull MultipartFile file){
        byte[] b;
        final String contentType = file.getContentType() == null ?
                null : file.getContentType().toLowerCase();
        try {
            final byte[] originalBytes = file.getBytes();
             b = Arrays.copyOf(originalBytes, originalBytes.length);
             /*I'm copying the bytes outside the thread to ensure the file bytes are fully loaded into mem, before it goes out of the thread's scope and is cleaned up
               I'm not too sure how to stop the tomcat no such file ex without. I don't think there's a solid fix online to this yet from what I've seen
             */
        } catch (IOException e) {
            log.info("An unexpected error occurred while trying to read this file", e);
            throw new FileReadException("An unexpected error occurred while trying to read this file", e);
        }

        return CompletableFuture.supplyAsync(() -> {
            if (file.isEmpty()) {
                throw new UnsupportedFileFormatException("File cannot be empty");
            }

            log.info("File content type: {}", contentType);

            if (contentType == null || !this.configProperties.getAllowedFileFormats().contains(contentType)) {
                log.info("Invalid file format");
                throw new UnsupportedFileFormatException("Invalid file format given. Format: %s".formatted(contentType));
            }

            return new FileWrapper(b, contentType);

        }, this.vExecutorService);

    }




}
