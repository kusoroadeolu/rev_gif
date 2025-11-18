package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.dtos.gif.BatchGifSearchCompletedEvent;
import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FileWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.model.Frame;
import com.github.kusoroadeolu.revgif.model.Gif;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    private final GifClient gifClient;
    private final GifCommandService gifCommandService;
    private final TaskExecutor taskExecutor;  //Virtual thread task exec

    public void orchestrate(byte[] b, String session){
         final FileWrapper fileWrapper = this.validatorService.validateFile(b);
         final List<FrameWrapper> frameWrappers = this.frameExtractor.extractFrames(fileWrapper);
         final List<HashWrapper> hashWrappers = this.hashingService.hashFrames(frameWrappers);

         for (final HashWrapper hw : hashWrappers) {
              CompletableFuture<Void> v = CompletableFuture.runAsync(() -> {
                 final BatchGifSearchCompletedEvent result = this.gifQueryService.findGifsFromDb(hw, session);
                 if(result.completedEventList().isEmpty()){
                     final ImageClientResponse clientResponse = this.imageClient.getFrameDescription(hw);
                     this.gifClient.getGifs(clientResponse, session);
                 }
             }, this.taskExecutor)
                     .whenComplete((result, e) -> {
                         if(e != null)log.error("An unexpected error occurred", e);
                     });
         }


    }

    public void testDb(){
        var v = getDummyGifs();
        this.gifCommandService.batchSave(v);
    }

    /**
     * Generates a list of dummy Gif objects for testing.
     *
     * @return A List of Gif objects.
     */
    public static List<Gif> getDummyGifs() {
        // --- Frames for Gif 1 (The Mountain) ---
        Frame frame1A = Frame.builder()
                .id(101L)
                .pHash(1234567890L)
                .frameIdx(0)
                .gif(1L)
                .build();
        Frame frame1B = Frame.builder()
                .id(102L)
                .pHash(9876543210L)
                .frameIdx(1)
                .gif(1L)
                .build();

        // --- Frames for Gif 2 (Coding Session) ---
        Frame frame2A = Frame.builder()
                .id(201L)
                .pHash(1122334455L)
                .frameIdx(0)
                .gif(2L)
                .build();
        Frame frame2B = Frame.builder()
                .id(202L)
                .pHash(6677889900L)
                .frameIdx(1)
                .gif(2L)
                .build();
        Frame frame2C = Frame.builder()
                .id(203L)
                .pHash(5432109876L)
                .frameIdx(2)
                .gif(2L)
                .build();

        // --- Frames for Gif 3 (Coffee Break) ---
        Frame frame3A = Frame.builder()
                .id(301L)
                .pHash(1020304050L)
                .frameIdx(0)
                .gif(3L)
                .build();


        // --- Gif Objects ---

        // 1. Mountain View GIF
        Gif gif1 = Gif.builder()
                .id(1L)
                .mimeType("image/gif")
                .createdAt(LocalDateTime.of(2024, 10, 15, 10, 30))
                .updatedAt(LocalDateTime.of(2024, 10, 15, 10, 30))
                .description("A looping gif of a mountain range at sunrise.")
                .tenorId("mXyZ9oQpVrS")
                .tenorUrl("https://tenor.com/view/mountain-sunrise")
                .searchQuery("mountain sunrise loop")
                .frames(Set.of(frame1A, frame1B))
                .build();

        // 2. Coding Session GIF
        Gif gif2 = Gif.builder()
                .id(2L)
                .mimeType("image/gif")
                .createdAt(LocalDateTime.of(2024, 11, 10, 14, 0))
                .updatedAt(LocalDateTime.of(2024, 11, 12, 9, 45)) // Updated later
                .description("Developer typing rapidly on a keyboard.")
                .tenorId("lPqR3sTuVwX")
                .tenorUrl("https://tenor.com/view/developer-typing")
                .searchQuery("coding developer fast typing")
                .frames(Set.of(frame2A, frame2B, frame2C))
                .build();

        // 3. Coffee Spill GIF
        Gif gif3 = Gif.builder()
                .id(3L)
                .mimeType("image/mp4") // Some GIFs are served as mp4/webm
                .createdAt(LocalDateTime.of(2024, 9, 5, 8, 15))
                .updatedAt(LocalDateTime.of(2024, 9, 5, 8, 15))
                .description("A mug of coffee spilling in slow motion.")
                .tenorId("aBcD1eFgH2i")
                .tenorUrl("https://tenor.com/view/coffee-spill")
                .searchQuery("coffee mug spill slow motion")
                .frames(Set.of(frame3A))
                .build();

        return List.of(gif1, gif2, gif3);
    }


}
