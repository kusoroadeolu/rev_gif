package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.exceptions.FileReadException;
import com.github.kusoroadeolu.revgif.model.FrameWrapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

@Service
@Slf4j
@RequiredArgsConstructor
public class FrameExtractor {

    private final ExecutorService vExecutorService;

    private final AppConfigProperties configProperties;

    public CompletableFuture<List<FrameWrapper>> extractFrames(MultipartFile file){
        return CompletableFuture.supplyAsync(() -> {
            List<FrameWrapper> frames = new ArrayList<>();
            try(final InputStream stream = file.getInputStream()){
                final String imageType = this.extractContentEnd(file.getContentType());
                final ImageReader reader = ImageIO.getImageReadersByFormatName(imageType)
                        .next();
                reader.setInput(stream);

                this.extractByType(imageType, reader, frames);
                return frames;
            }catch (IOException e){
                log.error("An unexpected error occurred while trying to read this file", e);
                throw new FileReadException("An unexpected error occurred while trying to read this file", e);
            }
        },
                this.vExecutorService);
    }

    private void extractByType(String type, ImageReader reader ,List<FrameWrapper> frames) throws IOException {
        switch (type){
            case "png", "webp", "jpeg" -> this.extractFrameFromImage(reader, frames, 0);
            case "gif" -> this.extractFramesFromGif(reader, frames);
        }
    }

    private void extractFramesFromGif(ImageReader reader, List<FrameWrapper> frames) throws IOException {
        final int numImgs = reader.getNumImages(true);
        final int nextFrame = numImgs / this.configProperties.getFrameNums();
        for (int i = 0; i < numImgs; i += nextFrame){
            this.extractFrameFromImage(reader, frames, i);
        }
    }

    private void extractFrameFromImage(ImageReader reader, List<FrameWrapper> frames, int idx) throws IOException {
        frames.add(new FrameWrapper(idx, reader.read(idx)));
    }

    private String extractContentEnd(@NonNull String contentType){
        return contentType.split("/")[1];
    }

}
