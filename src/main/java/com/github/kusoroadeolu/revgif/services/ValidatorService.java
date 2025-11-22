package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FileWrapper;
import com.github.kusoroadeolu.revgif.exceptions.UnsupportedFileFormatException;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ValidatorService {

    private final AppConfigProperties configProperties;
    private final Tika tika;
    private final LogMapper logMapper;
    private final static String CLASS_NAME = ValidatorService.class.getSimpleName();


    public FileWrapper validateFile(byte[] b){
        var allowedFormats = this.configProperties.allowedFileFormats();
        if(b.length < 1){
            throw new UnsupportedFileFormatException("File cannot be empty");
        }

        String mimeType;

        try {
            mimeType = tika.detect(b);
        }catch (Exception e){
            log.error(this.logMapper.log(CLASS_NAME, "Failed to detect file type"), e);
            throw new UnsupportedFileFormatException("Could not determine file type", e);
        }

        log.info(this.logMapper.log(CLASS_NAME, "File content type: %s".formatted(mimeType)));

        if (mimeType == null || !allowedFormats.contains(mimeType)) {
            throw new UnsupportedFileFormatException(
                    "Invalid file format. Detected: %s, Allowed: %s"
                            .formatted(mimeType, allowedFormats)
            );        }

        return new FileWrapper(b, mimeType);

    }




}
