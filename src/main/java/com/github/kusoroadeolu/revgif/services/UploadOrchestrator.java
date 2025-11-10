package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.services.impl.FrameExtractorServiceImpl;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadOrchestrator {

    private final FrameExtractorServiceImpl frameExtractor;
    private final HashingService hashingService;
    private final ValidatorService validatorService;


    public void orchestrate(@NonNull MultipartFile file){
         this.validatorService.validateFile(file)
                 .thenComposeAsync(this.frameExtractor::extractFrames)
                 .thenComposeAsync(this.hashingService::hashFrames);
    }

}
