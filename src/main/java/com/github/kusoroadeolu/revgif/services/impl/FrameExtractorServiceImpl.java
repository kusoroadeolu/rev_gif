package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FileWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.exceptions.FileReadException;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.services.FrameExtractorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class FrameExtractorServiceImpl implements FrameExtractorService {

    private final AppConfigProperties configProperties;
    private final LogMapper logMapper;
    private final static String CLASS_NAME = FrameExtractorServiceImpl.class.getSimpleName();
    private final static int IMAGE_START_IDX = 0;

    @Override
    public List<FrameWrapper> extractFrames(FileWrapper fileWrapper){
            final String format = this.getFileFormat(fileWrapper.contentType());
            return this.extractFramesFromMedia(fileWrapper.bytes(), format, ExtractionType.FROM_UPLOAD);
    }

    @Override
    public List<FrameWrapper> extractFrames(byte[] b, @NonNull String format){
        if(b.length == 0)return List.of();
        return this.extractFramesFromMedia(b, format, ExtractionType.FROM_TENOR);
    }


    public List<FrameWrapper> extractFramesFromMedia(byte[] b, String format, ExtractionType type){
        try(InputStream stream = new ByteArrayInputStream(b)){
            final ImageInputStream imageStream = ImageIO.createImageInputStream(stream);
            final ImageReader reader = ImageIO.getImageReadersByFormatName(format).next();
            reader.setInput(imageStream);
            final int frameNums = reader.getNumImages(true);
            log.info("Num of frames: {}", frameNums);

            if(frameNums > 1){
                return switch (type){
                    case FROM_TENOR -> this.getAllFrames(reader, frameNums, format);
                    case FROM_UPLOAD -> this.getFrames(reader, frameNums, format);
                };
            }

            final BufferedImage image = reader.read(IMAGE_START_IDX);
            return List.of(new FrameWrapper(IMAGE_START_IDX, image, format));
        }catch (IOException e){
            log.info(this.logMapper.log(CLASS_NAME, "An IO ex occurred while trying to extract frames from file. Content type: %s".formatted(format)));
            throw new FileReadException("An IO exception occurred while trying to extract frames from file. Content type: %s".formatted(format), e);
        }
    }

    private List<FrameWrapper> getFrames(ImageReader imageReader, int frameNums, String format) throws IOException {
        final int nextFrame = frameNums / this.configProperties.getExpectedFrames();
        return this.loopThroughImage(imageReader, format, nextFrame, frameNums);
    }

    private List<FrameWrapper> getAllFrames(ImageReader imageReader, int frameNums, String format) throws IOException {
        return this.loopThroughImage(imageReader, format, 1, frameNums);
    }

    private List<FrameWrapper> loopThroughImage(ImageReader imageReader, String format, int nextFrame, int frameNums) throws IOException{
        final List<FrameWrapper> wrappers = new ArrayList<>();
        BufferedImage bufferedImage;
        for (int i = 0; i < frameNums; i+=nextFrame){
            bufferedImage = imageReader.read(i);
            wrappers.add(new FrameWrapper(i, bufferedImage, format));
        }
        return wrappers;
    }

    private String getFileFormat(String contentType){
       return contentType.split("/")[1];
    }

    enum ExtractionType {
        FROM_UPLOAD,  //For uploads only extract a few frames across the gif/image
        FROM_TENOR   //For tenor gifs extract all frames
    }



}
