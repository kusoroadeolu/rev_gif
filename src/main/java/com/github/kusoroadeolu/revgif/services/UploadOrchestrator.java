package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.DbQueryResult;
import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;
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
import java.util.Set;
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
    private final FrameQueryService frameQueryService;
    private final GifClient gifClient;
    private final TaskExecutor taskExecutor;  //Virtual thread task exec


    public void orchestrate(byte[] b){
         final FileWrapper fileWrapper = this.validatorService.validateFile(b);
         final List<FrameWrapper> frameWrappers = this.frameExtractor.extractFrames(fileWrapper);
         final List<HashWrapper> hashWrappers = this.hashingService.hashFrames(frameWrappers);

         for (HashWrapper hw : hashWrappers) {
             CompletableFuture.runAsync(() -> {
                 final Set<DbQueryResult> result = this.frameQueryService.findGifsFromDb(hw);
                 if(result.isEmpty()){
                     final ImageClientResponse clientResponse = this.imageClient.getFrameDescription(hw);
                     this.gifClient.getGifs(clientResponse);
                 }
             }, this.taskExecutor)
                     .exceptionally(e ->  {
                         log.error("An unexpected error occurred", e);
                         return null;
             });
         }

    }


}
