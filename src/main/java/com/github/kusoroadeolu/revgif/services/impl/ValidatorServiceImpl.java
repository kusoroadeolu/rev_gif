package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.exceptions.FileReadException;
import com.github.kusoroadeolu.revgif.exceptions.UnsupportedFileFormatException;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FileWrapper;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.services.ValidatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
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
    private final Tika tika;
    private final LogMapper logMapper;
    private final static String CLASS_NAME = ValidatorServiceImpl.class.getSimpleName();

    @Override
    public FileWrapper validateFile(byte[] b){

        if(b.length < 1){
            throw new UnsupportedFileFormatException("File cannot be empty");
        }

        String mimeType = null;

        try {
            mimeType = tika.detect(b);
        }catch (Exception e){
            log.error(this.logMapper.log(CLASS_NAME, "Failed to detect file type"), e);
            throw new UnsupportedFileFormatException("Could not determine file type");
        }

        log.info(this.logMapper.log(CLASS_NAME, "File content type: %s".formatted(mimeType)));

        if (mimeType == null || !this.configProperties.getAllowedFileFormats().contains(mimeType)) {
            throw new UnsupportedFileFormatException(
                    "Invalid file format. Detected: %s, Allowed: %s"
                            .formatted(mimeType, configProperties.getAllowedFileFormats())
            );        }

        return new FileWrapper(b, mimeType);

    }




}
