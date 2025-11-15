package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.wrappers.FileWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
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

    private final FrameExtractorService frameExtractor;
    private final HashingService hashingService;
    private final ValidatorService validatorService;
    private final ImageClient imageClient;


    public void orchestrate(@NonNull MultipartFile file){
         final FileWrapper fileWrapper = this.validatorService.validateFile(file);
         final List<FrameWrapper> frameWrappers = this.frameExtractor.extractFrames(fileWrapper);
         final List<HashWrapper> hashWrappers = this.hashingService.hashFrames(frameWrappers);
        CompletableFuture<Void> future = new CompletableFuture<>();

         for (HashWrapper hw : hashWrappers){
             this.imageClient.getFrameDescription(hw);
         }

    }

}
