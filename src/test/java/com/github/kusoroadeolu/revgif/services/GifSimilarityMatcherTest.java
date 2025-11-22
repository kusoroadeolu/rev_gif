package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;
import com.github.kusoroadeolu.revgif.dtos.events.BatchGifSearchCompletedEvent;
import com.github.kusoroadeolu.revgif.dtos.gif.BatchDownloadedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.DownloadedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.HashedGif;
import com.github.kusoroadeolu.revgif.dtos.gif.NormalizedGif;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.mappers.FrameMapper;
import com.github.kusoroadeolu.revgif.mappers.GifMapper;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.model.Frame;
import com.github.kusoroadeolu.revgif.model.Gif;
import dev.brachtendorf.jimagehash.hash.Hash;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GifSimilarityMatcherTest {

    @Mock
    private FrameExtractorService frameExtractorService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private HashingService hashingService;

    @Mock
    private AppConfigProperties appConfigProperties;

    @Mock
    private GifCommandService gifCommandService;

    @Mock
    private FrameMapper frameMapper;

    @Mock
    private GifMapper gifMapper;

    @Mock
    private LogMapper logMapper;

    @Mock
    private Hash mockHash;

    @Captor
    private ArgumentCaptor<List<Gif>> gifListCaptor;

    @Captor
    private ArgumentCaptor<BatchGifSearchCompletedEvent> eventCaptor;

    private GifSimilarityMatcher gifSimilarityMatcher;

    @BeforeEach
    void setUp() {
        TaskExecutor taskExecutor = new SyncTaskExecutor();
        gifSimilarityMatcher = new GifSimilarityMatcher(
                frameExtractorService,
                applicationEventPublisher,
                hashingService,
                appConfigProperties,
                gifCommandService,
                frameMapper,
                gifMapper,
                logMapper,
                taskExecutor
        );

        lenient().when(appConfigProperties.nmHammingThreshold()).thenReturn(0.15);
        lenient().when(logMapper.log(anyString(), anyString())).thenReturn("Logged");
    }


    @Test
    void extractAndHash_withSimilarGifs_shouldProcessAndSave() {
        // Given
        String session = "test-session";
        String query = "funny cats";
        String format = "gif";

        BatchDownloadedGif batchDownloadedGif = createBatchDownloadedGif(query, format, 2);

        List<FrameWrapper> frames = createFrameWrappers(2);
        when(frameExtractorService.extractFrames(any(byte[].class), eq(format)))
                .thenReturn(frames);

        List<HashWrapper> hashWrappers = createHashWrappers(frames, 0.1);
        when(hashingService.hashFrames(any())).thenReturn(hashWrappers);

        Frame mockFrame = createFrame(1, 0.1);
        when(frameMapper.toFrame(any(HashWrapper.class), anyDouble())).thenReturn(mockFrame);

        Gif mockGif = createGif(1L, query, format);
        when(gifMapper.toGif(any(HashedGif.class), eq(query), eq(format), any()))
                .thenReturn(mockGif);
        when(gifMapper.toSearchCompletedEvent(anyList())).thenReturn(List.of());

        // When
        gifSimilarityMatcher.extractAndHash(batchDownloadedGif, session);

        // Then
        verify(frameExtractorService, times(2)).extractFrames(any(byte[].class), eq(format));
        verify(hashingService, times(2)).hashFrames(any());
        verify(gifCommandService).batchSave(any());
        verify(applicationEventPublisher).publishEvent(any(BatchGifSearchCompletedEvent.class));
    }

    @Test
    void extractAndHash_withNoSimilarGifs_shouldSaveEmptyList() {
        // Given
        String session = "test-session";
        String query = "dogs";
        String format = "gif";

        BatchDownloadedGif batchDownloadedGif = createBatchDownloadedGif(query, format, 2);

        List<FrameWrapper> frames = createFrameWrappers(2);
        when(frameExtractorService.extractFrames(any(byte[].class), eq(format)))
                .thenReturn(frames);

        List<HashWrapper> hashWrappers = createHashWrappers(frames, 0.9);
        when(hashingService.hashFrames(any())).thenReturn(hashWrappers);
        when(gifMapper.toSearchCompletedEvent(anyList())).thenReturn(List.of());

        // When
        gifSimilarityMatcher.extractAndHash(batchDownloadedGif, session);

        // Then
        verify(gifCommandService).batchSave(gifListCaptor.capture());
        List<Gif> savedGifs = gifListCaptor.getValue();
        assertTrue(savedGifs.isEmpty());
    }

    @Test
    void extractAndHash_withMixedSimilarity_shouldOnlySaveSimilarGifs() {
        // Given
        String session = "test-session";
        String query = "funny";
        String format = "gif";

        BatchDownloadedGif batchDownloadedGif = createBatchDownloadedGif(query, format, 3);

        List<FrameWrapper> frames = createFrameWrappers(2);
        when(frameExtractorService.extractFrames(any(byte[].class), eq(format)))
                .thenReturn(frames);

        List<HashWrapper> similarHashWrappers = createHashWrappers(frames, 0.1);
        List<HashWrapper> dissimilarHashWrappers = createHashWrappers(frames, 0.9);
        List<HashWrapper> similarHashWrappers2 = createHashWrappers(frames, 0.05);

        when(hashingService.hashFrames(any()))
                .thenReturn(similarHashWrappers)
                .thenReturn(dissimilarHashWrappers)
                .thenReturn(similarHashWrappers2);

        Frame mockFrame = createFrame(1, 0.1);
        when(frameMapper.toFrame(any(HashWrapper.class), anyDouble())).thenReturn(mockFrame);

        Gif mockGif1 = createGif(1L, query, format);
        Gif mockGif2 = createGif(2L, query, format);
        when(gifMapper.toGif(any(HashedGif.class), eq(query), eq(format), any()))
                .thenReturn(mockGif1)
                .thenReturn(mockGif2);
        when(gifMapper.toSearchCompletedEvent(anyList())).thenReturn(List.of());

        // When
        gifSimilarityMatcher.extractAndHash(batchDownloadedGif, session);

        // Then
        verify(gifCommandService).batchSave(gifListCaptor.capture());
        List<Gif> savedGifs = gifListCaptor.getValue();
        assertEquals(2, savedGifs.size());
    }

    @Test
    void extractAndHash_shouldPublishEventAfterProcessing() {
        // Given
        String session = "unique-session-123";
        String query = "test";
        String format = "gif";

        BatchDownloadedGif batchDownloadedGif = createBatchDownloadedGif(query, format, 1);

        List<FrameWrapper> frames = createFrameWrappers(1);
        when(frameExtractorService.extractFrames(any(byte[].class), eq(format)))
                .thenReturn(frames);

        List<HashWrapper> hashWrappers = createHashWrappers(frames, 0.1);
        when(hashingService.hashFrames(any())).thenReturn(hashWrappers);

        Frame mockFrame = createFrame(1, 0.1);
        when(frameMapper.toFrame(any(HashWrapper.class), anyDouble())).thenReturn(mockFrame);

        Gif mockGif = createGif(1L, query, format);
        when(gifMapper.toGif(any(HashedGif.class), eq(query), eq(format), any()))
                .thenReturn(mockGif);
        when(gifMapper.toSearchCompletedEvent(anyList())).thenReturn(List.of());

        // When
        gifSimilarityMatcher.extractAndHash(batchDownloadedGif, session);

        // Then
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
        assertNotNull(eventCaptor.getValue());
    }

    @Test
    void extractAndHash_withEmptyBatch_shouldHandleGracefully() {
        // Given
        String session = "test-session";
        BatchDownloadedGif batchDownloadedGif = createBatchDownloadedGif("query", "gif", 0);
        when(gifMapper.toSearchCompletedEvent(anyList())).thenReturn(List.of());

        // When
        gifSimilarityMatcher.extractAndHash(batchDownloadedGif, session);

        // Then
        verify(frameExtractorService, never()).extractFrames(any(byte[].class), anyString());
        verify(gifCommandService).batchSave(gifListCaptor.capture());
        assertTrue(gifListCaptor.getValue().isEmpty());
    }

    @Test
    void extractAndHash_withMultipleFramesPerGif_shouldProcessAllFrames() {
        // Given
        String session = "test-session";
        String query = "animation";
        String format = "gif";

        BatchDownloadedGif batchDownloadedGif = createBatchDownloadedGif(query, format, 1);

        List<FrameWrapper> frames = createFrameWrappers(5);
        when(frameExtractorService.extractFrames(any(byte[].class), eq(format)))
                .thenReturn(frames);

        List<HashWrapper> hashWrappers = createHashWrappers(frames, 0.1);
        when(hashingService.hashFrames(any())).thenReturn(hashWrappers);

        Frame mockFrame = createFrame(1, 0.1);
        when(frameMapper.toFrame(any(HashWrapper.class), anyDouble())).thenReturn(mockFrame);

        Gif mockGif = createGif(1L, query, format);
        when(gifMapper.toGif(any(HashedGif.class), eq(query), eq(format), any()))
                .thenReturn(mockGif);
        when(gifMapper.toSearchCompletedEvent(anyList())).thenReturn(List.of());

        // When
        gifSimilarityMatcher.extractAndHash(batchDownloadedGif, session);

        // Then
        verify(hashingService).hashFrames(argThat(list -> list.size() == 5));
        verify(frameMapper, times(5)).toFrame(any(HashWrapper.class), anyDouble());
    }

    @Test
    void extractAndHash_withDifferentThreshold_shouldRespectThreshold() {
        // Given
        when(appConfigProperties.nmHammingThreshold()).thenReturn(0.05);

        String session = "test-session";
        String query = "strict";
        String format = "gif";

        BatchDownloadedGif batchDownloadedGif = createBatchDownloadedGif(query, format, 1);

        List<FrameWrapper> frames = createFrameWrappers(2);
        when(frameExtractorService.extractFrames(any(byte[].class), eq(format)))
                .thenReturn(frames);

        List<HashWrapper> hashWrappers = new ArrayList<>();
        hashWrappers.add(createHashWrapper(frames.get(0), 0.04));
        hashWrappers.add(createHashWrapper(frames.get(1), 0.06));
        when(hashingService.hashFrames(any())).thenReturn(hashWrappers);

        Frame mockFrame = createFrame(1, 0.04);
        when(frameMapper.toFrame(any(HashWrapper.class), anyDouble())).thenReturn(mockFrame);

        Gif mockGif = createGif(1L, query, format);
        when(gifMapper.toGif(any(HashedGif.class), eq(query), eq(format), any()))
                .thenReturn(mockGif);
        when(gifMapper.toSearchCompletedEvent(anyList())).thenReturn(List.of());

        // When
        gifSimilarityMatcher.extractAndHash(batchDownloadedGif, session);

        // Then
        verify(frameMapper, times(1)).toFrame(any(HashWrapper.class), eq(0.04));
        verify(frameMapper, never()).toFrame(any(HashWrapper.class), eq(0.06));
    }

    @Test
    void extractAndHash_withZeroThreshold_shouldOnlyMatchExactMatches() {
        // Given
        when(appConfigProperties.nmHammingThreshold()).thenReturn(0.0);

        String session = "test-session";
        String query = "exact";
        String format = "gif";

        BatchDownloadedGif batchDownloadedGif = createBatchDownloadedGif(query, format, 1);

        List<FrameWrapper> frames = createFrameWrappers(1);
        when(frameExtractorService.extractFrames(any(byte[].class), eq(format)))
                .thenReturn(frames);

        List<HashWrapper> hashWrappers = createHashWrappers(frames, 0.0);
        when(hashingService.hashFrames(any())).thenReturn(hashWrappers);

        Frame mockFrame = createFrame(1, 0.0);
        when(frameMapper.toFrame(any(HashWrapper.class), anyDouble())).thenReturn(mockFrame);

        Gif mockGif = createGif(1L, query, format);
        when(gifMapper.toGif(any(HashedGif.class), eq(query), eq(format), any()))
                .thenReturn(mockGif);
        when(gifMapper.toSearchCompletedEvent(anyList())).thenReturn(List.of());

        // When
        gifSimilarityMatcher.extractAndHash(batchDownloadedGif, session);

        // Then
        verify(frameMapper).toFrame(any(HashWrapper.class), eq(0.0));
        verify(gifCommandService).batchSave(gifListCaptor.capture());
        assertEquals(1, gifListCaptor.getValue().size());
    }

    @Test
    void extractAndHash_withSingleFrameGifs_shouldProcessCorrectly() {
        // Given
        String session = "test-session";
        String query = "single-frame";
        String format = "png";

        BatchDownloadedGif batchDownloadedGif = createBatchDownloadedGif(query, format, 2);

        List<FrameWrapper> frames = createFrameWrappers(1);
        when(frameExtractorService.extractFrames(any(byte[].class), eq(format)))
                .thenReturn(frames);

        List<HashWrapper> hashWrappers = createHashWrappers(frames, 0.1);
        when(hashingService.hashFrames(any())).thenReturn(hashWrappers);

        Frame mockFrame = createFrame(1, 0.1);
        when(frameMapper.toFrame(any(HashWrapper.class), anyDouble())).thenReturn(mockFrame);

        Gif mockGif1 = createGif(1L, query, format);
        Gif mockGif2 = createGif(2L, query, format);
        when(gifMapper.toGif(any(HashedGif.class), eq(query), eq(format), any()))
                .thenReturn(mockGif1)
                .thenReturn(mockGif2);
        when(gifMapper.toSearchCompletedEvent(anyList())).thenReturn(List.of());

        // When
        gifSimilarityMatcher.extractAndHash(batchDownloadedGif, session);

        // Then
        verify(gifCommandService).batchSave(gifListCaptor.capture());
        assertEquals(2, gifListCaptor.getValue().size());
    }

    @Test
    void extractAndHash_whenAllHashComparisonsWithGif_shouldNotSaveThatGif() {
        // Given
        String session = "test-session";
        String query = "test";
        String format = "gif";

        BatchDownloadedGif batchDownloadedGif = createBatchDownloadedGif(query, format, 1);

        List<FrameWrapper> frames = createFrameWrappers(2);
        when(frameExtractorService.extractFrames(any(byte[].class), eq(format)))
                .thenReturn(frames);

        // All hashes throw exceptions
        Hash problematicHash = mock(Hash.class);
        when(problematicHash.normalizedHammingDistance(any()))
                .thenThrow(new RuntimeException("Hash comparison failed"));

        List<HashWrapper> hashWrappers = new ArrayList<>();
        hashWrappers.add(new HashWrapper(frames.get(0), problematicHash));
        hashWrappers.add(new HashWrapper(frames.get(1), problematicHash));
        when(hashingService.hashFrames(any())).thenReturn(hashWrappers);
        when(gifMapper.toSearchCompletedEvent(anyList())).thenReturn(List.of());

        // When
        gifSimilarityMatcher.extractAndHash(batchDownloadedGif, session);

        // Then
        verify(gifCommandService).batchSave(gifListCaptor.capture());
        assertTrue(gifListCaptor.getValue().isEmpty());
    }

    @Test
    void extractAndHash_withLargeNumberOfGifs_shouldProcessAll() {
        // Given
        String session = "test-session";
        String query = "many gifs";
        String format = "gif";

        BatchDownloadedGif batchDownloadedGif = createBatchDownloadedGif(query, format, 10);

        List<FrameWrapper> frames = createFrameWrappers(1);
        when(frameExtractorService.extractFrames(any(byte[].class), eq(format)))
                .thenReturn(frames);

        List<HashWrapper> hashWrappers = createHashWrappers(frames, 0.1);
        when(hashingService.hashFrames(any())).thenReturn(hashWrappers);

        Frame mockFrame = createFrame(1, 0.1);
        when(frameMapper.toFrame(any(HashWrapper.class), anyDouble())).thenReturn(mockFrame);

        when(gifMapper.toGif(any(HashedGif.class), eq(query), eq(format), any()))
                .thenAnswer(invocation -> createGif((long) Math.random() * 1000, query, format));
        when(gifMapper.toSearchCompletedEvent(anyList())).thenReturn(List.of());

        // When
        gifSimilarityMatcher.extractAndHash(batchDownloadedGif, session);

        // Then
        verify(frameExtractorService, times(10)).extractFrames(any(byte[].class), eq(format));
        verify(gifCommandService).batchSave(gifListCaptor.capture());
        assertEquals(10, gifListCaptor.getValue().size());
    }

    @Test
    void extractAndHash_verifyFrameMapperCalledWithCorrectDistance() {
        // Given
        String session = "test-session";
        String query = "test";
        String format = "gif";
        double expectedDistance = 0.12345;

        BatchDownloadedGif batchDownloadedGif = createBatchDownloadedGif(query, format, 1);

        List<FrameWrapper> frames = createFrameWrappers(1);
        when(frameExtractorService.extractFrames(any(byte[].class), eq(format)))
                .thenReturn(frames);

        List<HashWrapper> hashWrappers = createHashWrappers(frames, expectedDistance);
        when(hashingService.hashFrames(any())).thenReturn(hashWrappers);

        Frame mockFrame = createFrame(1, expectedDistance);
        when(frameMapper.toFrame(any(HashWrapper.class), anyDouble())).thenReturn(mockFrame);

        Gif mockGif = createGif(1L, query, format);
        when(gifMapper.toGif(any(HashedGif.class), eq(query), eq(format), any()))
                .thenReturn(mockGif);
        when(gifMapper.toSearchCompletedEvent(anyList())).thenReturn(List.of());

        // When
        gifSimilarityMatcher.extractAndHash(batchDownloadedGif, session);

        // Then
        verify(frameMapper).toFrame(any(HashWrapper.class), eq(expectedDistance));
    }

    // ============= HELPER METHODS =============

    private BatchDownloadedGif createBatchDownloadedGif(String query, String format, int count) {
        List<DownloadedGif> downloadedGifs = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            NormalizedGif normalizedGif = new NormalizedGif(
                    "gif-id-" + i,
                    "https://example.com/gif-" + i,
                    "Description " + i
            );
            downloadedGifs.add(new DownloadedGif(normalizedGif, new byte[]{1, 2, 3}));
        }

        ImageClientResponse clientResponse = new ImageClientResponse(query, format, 0, mockHash);
        return new BatchDownloadedGif(downloadedGifs, clientResponse);
    }

    private List<FrameWrapper> createFrameWrappers(int count) {
        List<FrameWrapper> frames = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            frames.add(new FrameWrapper(i, image, "gif"));
        }
        return frames;
    }

    private List<HashWrapper> createHashWrappers(List<FrameWrapper> frames, double distance) {
        List<HashWrapper> hashWrappers = new ArrayList<>();
        for (FrameWrapper frame : frames) {
            hashWrappers.add(createHashWrapper(frame, distance));
        }
        return hashWrappers;
    }

    private HashWrapper createHashWrapper(FrameWrapper frame, double distance) {
        Hash hash = mock(Hash.class);
        when(hash.normalizedHammingDistance(any(Hash.class))).thenReturn(distance);
        return new HashWrapper(frame, hash);
    }

    private Frame createFrame(int frameIdx, double distance) {
        return Frame.builder()
                .id((long) frameIdx)
                .frameIdx(frameIdx)
                .nmHammingDist(distance)
                .pHash(12345L)
                .build();
    }

    private Gif createGif(Long id, String query, String format) {
        return Gif.builder()
                .id(id)
                .searchQuery(query)
                .mimeType("image/" + format)
                .tenorId("tenor-" + id)
                .tenorUrl("https://tenor.com/" + id)
                .description("Test gif " + id)
                .frames(Set.of())
                .build();
    }
}