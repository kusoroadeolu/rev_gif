package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.exceptions.FileReadException;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FileWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.services.FrameExtractorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            final String contentType = this.extractContentEnd(fileWrapper.contentType());
            try{
                return this.extractFrameFromImage(fileWrapper, contentType);
            }catch (IOException e){
                log.info(this.logMapper.getLog(CLASS_NAME, "An IO ex occurred while trying to extract frames from file. Content type: %s".formatted(contentType)));
                throw new FileReadException("An IO exception occurred while trying to extract frames from file. Content type: %s".formatted(contentType), e);
            }
    }

    private List<FrameWrapper> extractFrameFromImage(FileWrapper fileWrapper, String contentType) throws IOException {
        try(InputStream stream = new ByteArrayInputStream(fileWrapper.bytes())){
            final ImageInputStream imageStream = ImageIO.createImageInputStream(stream);
            final ImageReader reader = ImageIO.getImageReadersByFormatName(contentType).next();
            reader.setInput(imageStream);
            final int frameNums = reader.getNumImages(true);

            if(frameNums > 1){
                return this.getAllFrames(reader, frameNums, contentType);
            }

            final BufferedImage image = reader.read(IMAGE_START_IDX);
            return List.of(new FrameWrapper(IMAGE_START_IDX, image, contentType));
        }
    }

    private List<FrameWrapper> getAllFrames(ImageReader imageReader, int frameNums, String contentType) throws IOException {
        final int nextFrame = frameNums / this.configProperties.getExpectedFrames();
        final List<FrameWrapper> wrappers = new ArrayList<>(this.configProperties.getExpectedFrames());
        BufferedImage bufferedImage;

        for (int i = 0; i < frameNums; i+=nextFrame){
             bufferedImage = imageReader.read(i);
             wrappers.add(new FrameWrapper(i, bufferedImage, contentType));
        }

        return wrappers;
    }

    private String extractContentEnd(String contentType){
        String val = contentType.split("/")[1];
        log.info("Content end: {}", val);
        return val;
    }

}
