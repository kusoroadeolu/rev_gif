package com.github.kusoroadeolu.revgif.services;

import com.github.kusoroadeolu.revgif.configprops.AppConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FileWrapper;
import com.github.kusoroadeolu.revgif.dtos.wrappers.FrameWrapper;
import com.github.kusoroadeolu.revgif.exceptions.FileReadException;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class FrameExtractorService {

    private final AppConfigProperties configProperties;
    private final LogMapper logMapper;
    private final static String CLASS_NAME = FrameExtractorService.class.getSimpleName();
    private final static int IMAGE_START_IDX = 0;

    public List<FrameWrapper> extractFrames(@NonNull FileWrapper fileWrapper){
            final String format = this.getFileFormat(fileWrapper.contentType());
            final byte[] b = fileWrapper.bytes();
            if (b.length == 0)return List.of();
            return this.extractFramesFromMedia(fileWrapper.bytes(), format, ExtractionType.FROM_UPLOAD);
    }

    public List<FrameWrapper> extractFrames(byte[] b, @NonNull String format){
        if(b.length == 0) return List.of();
        return this.extractFramesFromMedia(b, format, ExtractionType.FROM_TENOR);
    }


    private List<FrameWrapper> extractFramesFromMedia(byte[] b, String format, ExtractionType type){
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

            final BufferedImage image = this.applyBlur(reader.read(IMAGE_START_IDX), 3);
            return List.of(new FrameWrapper(IMAGE_START_IDX, image, format));
        }catch (IOException e){
            log.info(this.logMapper.log(CLASS_NAME, "An IO ex occurred while trying to extract frames from file. Content type: %s".formatted(format)));
            throw new FileReadException("An IO exception occurred while trying to extract frames from file. Content type: %s".formatted(format), e);
        }
    }

    private List<FrameWrapper> getFrames(ImageReader imageReader, int frameNums, String format) throws IOException {
        final int nextFrame = frameNums / this.configProperties.expectedFrames();
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


    //Apply a slight gaussian blur to this image
    private BufferedImage applyBlur(BufferedImage img, int radius){
        Kernel kernel = createGaussianKernel(radius);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_ZERO_FILL, null); // handles edges by filling with zeros
        // null creates a new destination image
        return op.filter(img, null);
    }

    private Kernel createGaussianKernel(int radius){
        int size = radius * 2 + 1;
        float[] data = new float[size * size];
        double sigma = radius / 3.0; // Standard deviation
        double twoSigmaSquare = 2.0 * sigma * sigma;
        float sum = 0;

        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                double exponent = -(x * x + y * y) / twoSigmaSquare;
                float weight = (float) Math.exp(exponent);
                data[(y + radius) * size + (x + radius)] = weight;
                sum += weight;
            }
        }

        // Normalize the kernel
        for (int i = 0; i < data.length; i++) {
            data[i] /= sum;
        }

        return new Kernel(size, size, data);
    }

    private String getFileFormat(String contentType){
       return contentType.split("/")[1];
    }

    private enum ExtractionType {
        FROM_UPLOAD,  //For uploads only extract a few frames across the gif/image
        FROM_TENOR   //For tenor gifs extract all frames
    }



}
