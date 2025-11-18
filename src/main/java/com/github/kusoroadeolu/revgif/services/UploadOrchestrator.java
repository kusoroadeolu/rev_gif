package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.events.BatchGifSearchCompletedEvent;
import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;
import com.github.kusoroadeolu.revgif.dtos.events.GifSearchErrorEvent;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FileWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.dtos.events.EventErrorType;
import com.github.kusoroadeolu.revgif.exceptions.ImageClientException;
import com.google.genai.errors.ApiException;
import com.google.genai.errors.GenAiIOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final GifQueryService gifQueryService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final GifClient gifClient;
    private final GifCommandService gifCommandService;
    private final TaskExecutor taskExecutor;  //Virtual thread task exec

    public void orchestrate(byte[] b, String session){
         final FileWrapper fileWrapper = this.validatorService.validateFile(b);
         final List<FrameWrapper> frameWrappers = this.frameExtractor.extractFrames(fileWrapper);
         final List<HashWrapper> hashWrappers = this.hashingService.hashFrames(frameWrappers);

         for (final HashWrapper hw : hashWrappers) {
              CompletableFuture<Void> v = CompletableFuture.runAsync(() -> {
                  try {
                      final BatchGifSearchCompletedEvent result = this.gifQueryService.findGifsFromDb(hw, session);
                      if (result.completedEventList().isEmpty()) {
                          final ImageClientResponse clientResponse = this.imageClient.getFrameDescription(hw);
                          this.gifClient.getGifs(clientResponse, session);
                      }
                  }catch (GenAiIOException | ImageClientException ex){
                      log.error("Failed to analyze given image.", ex);
                      this.applicationEventPublisher.publishEvent(new GifSearchErrorEvent("We failed to analyze your image", EventErrorType.IMAGE_ANALYSIS, session ,LocalDateTime.now()));
                  }catch (ApiException e){
                    log.error("Failed to reach gemini due to an API ex", e);
                    this.applicationEventPublisher.publishEvent(new GifSearchErrorEvent("Failed to reach gemini due to an API ex", EventErrorType.GEMINI_SERVER_ERR, session ,LocalDateTime.now()));
                  }
             }, this.taskExecutor)
                      .exceptionally(e -> {
                          log.error("An unexpected error occurred", e);
                          this.applicationEventPublisher.publishEvent(new GifSearchErrorEvent("An unexpected error occurred", EventErrorType.UNEXPECTED_ERR, session ,LocalDateTime.now()));
                          return null;
                      });
         }


    }


}
