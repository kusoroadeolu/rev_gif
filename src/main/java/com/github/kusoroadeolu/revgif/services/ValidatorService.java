package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.exceptions.UnsupportedFileFormatException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidatorService {

    private final AppConfigProperties configProperties;
    private final ExecutorService vExecutorService;

    public CompletableFuture<MultipartFile> validateFile(@NonNull MultipartFile file){
        return CompletableFuture.supplyAsync(() -> {
            if (file.isEmpty()) {
                throw new UnsupportedFileFormatException("File cannot be empty");
            }

            final String contentType = file.getContentType() == null ?
                    null : file.getContentType().toLowerCase();

            log.info("File content type: {}", contentType);

            if (contentType == null || !this.configProperties.getAllowedFileFormats().contains(contentType)) {
                throw new UnsupportedFileFormatException("Invalid file format given. Format: %s".formatted(contentType));
            }

            return file;
        }, this.vExecutorService);

    }




}
