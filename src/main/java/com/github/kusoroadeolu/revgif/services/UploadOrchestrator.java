package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.wrappers.FileWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import dev.brachtendorf.jimagehash.hash.Hash;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
@RequiredArgsConstructor
public class UploadOrchestrator {

    private final FrameExtractorService frameExtractor;
    private final HashingService hashingService;
    private final ValidatorService validatorService;
    private final ImageClient imageClient;
    private final GifClient gifClient;
    private final TaskExecutor taskExecutor;  //Virtual thread task exec


    public void orchestrate(@NonNull MultipartFile file){
         final FileWrapper fileWrapper = this.validatorService.validateFile(file);
         final List<FrameWrapper> frameWrappers = this.frameExtractor.extractFrames(fileWrapper);
         final List<HashWrapper> hashWrappers = this.hashingService.hashFrames(frameWrappers);

         for (HashWrapper hw : hashWrappers) {
             CompletableFuture.runAsync(() -> {
                 var v = this.imageClient.getFrameDescription(hw);
                 this.gifClient.getGifs(v);
             }, this.taskExecutor);
         }

    }

    @Async
    public void runAsync(HashWrapper hw){

    }

}
