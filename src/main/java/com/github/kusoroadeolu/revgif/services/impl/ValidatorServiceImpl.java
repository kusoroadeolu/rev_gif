package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.exceptions.FileReadException;
import com.github.kusoroadeolu.revgif.exceptions.UnsupportedFileFormatException;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FileWrapper;
import com.github.kusoroadeolu.revgif.services.ValidatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidatorServiceImpl implements ValidatorService {

    private final AppConfigProperties configProperties;

    @Override
    public FileWrapper validateFile(@NonNull MultipartFile file){
        byte[] b;
        final String contentType = file.getContentType() == null ?
                null : file.getContentType().toLowerCase();
        try {
            final byte[] originalBytes = file.getBytes();
             b = Arrays.copyOf(originalBytes, originalBytes.length);
        } catch (IOException e) {
            log.info("An unexpected error occurred while trying to read this file", e);
            throw new FileReadException("An unexpected error occurred while trying to read this file", e);
        }


        if (file.isEmpty()) {
            throw new UnsupportedFileFormatException("File cannot be empty");
        }

        log.info("File content type: {}", contentType);

        if (contentType == null || !this.configProperties.getAllowedFileFormats().contains(contentType)) {
            log.info("Invalid file format");
            throw new UnsupportedFileFormatException("Invalid file format given. Format: %s".formatted(contentType));
        }

        return new FileWrapper(b, contentType);

    }




}
