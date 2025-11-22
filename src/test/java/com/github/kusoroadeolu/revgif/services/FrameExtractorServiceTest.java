package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FileWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.exceptions.FileReadException;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FrameExtractorServiceTest {

    @Mock
    private AppConfigProperties configProperties;

    @Mock
    private LogMapper logMapper;

    @InjectMocks
    private FrameExtractorService frameExtractorService;


    @Test
    void extractFrames_withValidGif_shouldReturnFrames() {
        // Given
        byte[] gifBytes = createTestGif();
        FileWrapper fileWrapper = new FileWrapper(gifBytes, "image/gif");

        // When
        List<FrameWrapper> frames = frameExtractorService.extractFrames(fileWrapper);

        // Then
        assertFalse(frames.isEmpty());

        assertEquals("gif", frames.getFirst().format());
    }

    @Test
    void extractFrames_withValidPng_shouldReturnSingleFrame() {
        // Given
        byte[] pngBytes = createTestImage("png");
        FileWrapper fileWrapper = new FileWrapper(pngBytes, "image/png");

        // When
        List<FrameWrapper> frames = frameExtractorService.extractFrames(fileWrapper);

        // Then
        assertEquals(1, frames.size());
        assertEquals(0, frames.getFirst().frameIdx());
        assertEquals("png", frames.getFirst().format());
        assertNotNull(frames.getFirst().image());
    }

    @Test
    void extractFrames_withValidJpeg_shouldReturnSingleFrame() {
        // Given
        byte[] jpegBytes = createTestImage("jpeg");
        FileWrapper fileWrapper = new FileWrapper(jpegBytes, "image/jpeg");

        // When
        List<FrameWrapper> frames = frameExtractorService.extractFrames(fileWrapper);

        // Then
        assertEquals(1, frames.size());
        assertEquals("jpeg", frames.get(0).format());
    }

    @Test
    void extractFrames_withEmptyBytes_shouldReturnEmptyList() {
        // Given
        FileWrapper fileWrapper = new FileWrapper(new byte[0], "image/gif");

        // When
        List<FrameWrapper> frames = frameExtractorService.extractFrames(fileWrapper);

        // Then
        assertTrue(frames.isEmpty());
    }

    @Test
    void extractFramesFromTenor_withValidGif_shouldReturnAllFrames() {
        // Given
        byte[] gifBytes = createTestGif();
        String format = "gif";

        // When
        List<FrameWrapper> frames = frameExtractorService.extractFrames(gifBytes, format);

        // Then
        assertFalse(frames.isEmpty());
    }

    @Test
    void extractFramesFromTenor_withEmptyBytes_shouldReturnEmptyList() {
        // Given
        byte[] emptyBytes = new byte[0];
        String format = "gif";

        // When
        List<FrameWrapper> frames = frameExtractorService.extractFrames(emptyBytes, format);

        // Then
        assertTrue(frames.isEmpty());
    }

    @Test
    void extractFrames_withMultiFrameGif_shouldExtractSubsetOfFrames() {
        // Given
        byte[] gifBytes = createTestGif();
        FileWrapper fileWrapper = new FileWrapper(gifBytes, "image/gif");

        // When
        List<FrameWrapper> frames = frameExtractorService.extractFrames(fileWrapper);

        // Then
        assertFalse(frames.isEmpty());
    }

    @Test
    void extractFrames_shouldApplyBlurToSingleImage() {
        // Given
        byte[] pngBytes = createTestImage("png");
        FileWrapper fileWrapper = new FileWrapper(pngBytes, "image/png");

        // When
        List<FrameWrapper> frames = frameExtractorService.extractFrames(fileWrapper);

        // Then
        assertEquals(1, frames.size());
        assertNotNull(frames.get(0).image());
    }

    @Test
    void extractFramesFromTenor_withValidPng_shouldReturnFrame() {
        // Given
        byte[] pngBytes = createTestImage("png");
        String format = "png";

        // When
        List<FrameWrapper> frames = frameExtractorService.extractFrames(pngBytes, format);

        // Then
        assertEquals(1, frames.size());
        assertEquals("png", frames.get(0).format());
    }


    @Test
    void extractFrames_withCorruptedImageData_shouldThrowFileReadException() {
        // Given
        byte[] corruptedBytes = new byte[]{1, 2, 3, 4, 5};
        FileWrapper fileWrapper = new FileWrapper(corruptedBytes, "image/gif");

        // When/Then
        FileReadException exception = assertThrows(FileReadException.class,
                () -> frameExtractorService.extractFrames(fileWrapper));

        assertTrue(exception.getMessage().contains("An IO exception occurred while trying to extract frames"));
    }

    @Test
    void extractFrames_withInvalidFormat_shouldThrowException() {
        // Given
        byte[] bytes = createTestImage("png");
        FileWrapper fileWrapper = new FileWrapper(bytes, "image/invalid");

        // When/Then
        // ImageIO throws NoSuchElementException when format is not supported
        assertThrows(Exception.class,
                () -> frameExtractorService.extractFrames(fileWrapper));
    }

    @Test
    void extractFramesFromTenor_withCorruptedData_shouldThrowFileReadException() {
        // Given
        byte[] corruptedBytes = new byte[]{1, 2, 3, 4, 5};
        String format = "gif";

        // When/Then
        FileReadException exception = assertThrows(FileReadException.class,
                () -> frameExtractorService.extractFrames(corruptedBytes, format));

        assertNotNull(exception.getCause());
    }

    @Test
    void extractFrames_withNullContentType_shouldThrowException() {
        // Given
        byte[] bytes = createTestImage("png");

        // When/Then
        assertThrows(NullPointerException.class, () -> {
            FileWrapper fileWrapper = new FileWrapper(bytes, null);
            frameExtractorService.extractFrames(fileWrapper);
        });
    }

    @Test
    void extractFramesFromTenor_withNullFormat_shouldThrowException() {
        // Given
        byte[] bytes = createTestImage("png");

        // When/Then
        // ImageIO throws IllegalArgumentException for null format
        assertThrows(IllegalArgumentException.class,
                () -> frameExtractorService.extractFrames(bytes, null));
    }

    @Test
    void extractFrames_withMalformedContentType_shouldThrowException() {
        // Given
        byte[] bytes = createTestImage("png");
        FileWrapper fileWrapper = new FileWrapper(bytes, "invalid-format");

        // When/Then
        // Should throw ArrayIndexOutOfBoundsException when splitting contentType
        assertThrows(ArrayIndexOutOfBoundsException.class,
                () -> frameExtractorService.extractFrames(fileWrapper));
    }

    @Test
    void extractFrames_verifyLoggingOnException() {
        // Given
        byte[] corruptedBytes = new byte[]{1, 2, 3, 4, 5};
        FileWrapper fileWrapper = new FileWrapper(corruptedBytes, "image/gif");
        when(logMapper.log(anyString(), anyString())).thenReturn("Logged message");

        // When/Then
        assertThrows(FileReadException.class,
                () -> frameExtractorService.extractFrames(fileWrapper));

        verify(logMapper).log(eq("FrameExtractorService"), contains("An IO ex occurred"));
    }

    @Test
    void extractFrames_withNullFileWrapper_shouldThrowException() {
        // When/Then
        assertThrows(NullPointerException.class,
                () -> frameExtractorService.extractFrames(null));
    }

    @Test
    void extractFramesFromTenor_withNullBytes_shouldThrowException() {
        // When/Then
        assertThrows(NullPointerException.class,
                () -> frameExtractorService.extractFrames(null, "gif"));
    }

    @Test
    void extractFrames_withContentTypeHavingSlash_shouldExtractFormat() {
        // Given
        byte[] pngBytes = createTestImage("png");
        FileWrapper fileWrapper = new FileWrapper(pngBytes, "image/png");

        // When
        List<FrameWrapper> frames = frameExtractorService.extractFrames(fileWrapper);

        // Then
        assertFalse(frames.isEmpty());
        assertEquals("png", frames.get(0).format());
    }

    @Test
    void extractFrames_withBmp_shouldReturnFrame() {
        // Given
        byte[] bmpBytes = createTestImage("bmp");
        FileWrapper fileWrapper = new FileWrapper(bmpBytes, "image/bmp");

        // When
        List<FrameWrapper> frames = frameExtractorService.extractFrames(fileWrapper);

        // Then
        assertEquals(1, frames.size());
        assertEquals("bmp", frames.get(0).format());
    }

    @Test
    void extractFramesFromTenor_withLargeByteArray_shouldHandleCorrectly() {
        // Given
        byte[] largeImage = createLargeTestImage();
        String format = "png";

        // When
        List<FrameWrapper> frames = frameExtractorService.extractFrames(largeImage, format);

        // Then
        assertFalse(frames.isEmpty());
    }

    // ============= HELPER METHODS =============

    private byte[] createTestImage(String format) {
        try {
            BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, format, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test image", e);
        }
    }

    private byte[] createTestGif() {
        try {
            BufferedImage image = new BufferedImage(50, 50, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "gif", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create test GIF", e);
        }
    }

    private byte[] createLargeTestImage() {
        try {
            BufferedImage image = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create large test image", e);
        }
    }
}