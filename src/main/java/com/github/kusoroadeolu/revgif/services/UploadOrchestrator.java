package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.model.HashWrapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadOrchestrator {

    private final FrameExtractor frameExtractor;
    private final HashingService hashingService;
    private final ValidatorService validatorService;


    public CompletableFuture<List<HashWrapper>> orchestrate(@NonNull MultipartFile file){
        return this.validatorService.validateFile(file)
                .thenCompose(this.frameExtractor::extractFrames)
                .thenCompose(this.hashingService::hashFrames);
    }

}
