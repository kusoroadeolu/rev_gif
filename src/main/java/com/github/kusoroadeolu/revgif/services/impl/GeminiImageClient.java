package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.GeminiConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;
import com.github.kusoroadeolu.revgif.exceptions.FileReadException;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.exceptions.ImageClientException;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.services.ImageClient;
import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletionException;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeminiImageClient implements ImageClient {

    private final Client geminiClient;
    private final GeminiConfigProperties geminiConfigProperties;
    private final LogMapper logMapper;
    private final static String CLASS_NAME = GeminiImageClient.class.getSimpleName();
    private final static String DEFAULT_FORMAT = "png";
    private final static String DEFAULT_MIME_TYPE = "image/png";

    @Override
    public ImageClientResponse getFrameDescription(@NonNull HashWrapper wrapper) {
        try{
            final BufferedImage image = wrapper.frameWrapper().image();
            final byte[] b = this.toBytes(image);
            final Part imgPart = Part.fromBytes(b, DEFAULT_MIME_TYPE);
            final Part textPart = Part.fromText(PROMPT);
            final Tool googleSearchTool = Tool.builder()
                    .googleSearch(GoogleSearch.builder().build())
                    .build();

            final Content content = Content.fromParts(textPart, imgPart);
            final GenerateContentResponse description = this.geminiClient.models.generateContent(
                        this.geminiConfigProperties.model(),
                        content,
                        GenerateContentConfig.builder().tools(googleSearchTool).build()
            );

            if(description.text() == null || description.text().isEmpty()){
                log.error(this.logMapper.log(CLASS_NAME, "Received null or empty response from gemini. Retrying..."));
                throw new ImageClientException();
            }

            log.info(this.logMapper.log(CLASS_NAME, "Result: %s".formatted(description.text())));
            return new ImageClientResponse(description.text(), wrapper.frameWrapper().format(), wrapper.frameWrapper().frameIdx(), wrapper.hash());
        }catch (IOException e) {
            log.error(this.logMapper.log(CLASS_NAME, "An image read ex occurred."), e);
            throw new ImageClientException();
        }
    }

    //Helper method to convert a buf image to bytes
    private byte[] toBytes(BufferedImage image) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, DEFAULT_FORMAT, baos);
        return baos.toByteArray();
    }


    private final static String PROMPT =
        """
                Generate a 2-3 word GIF search query for this image.
               \s
                Priority order:
                1. Named people/celebrities if recognizable → pair with ONE generic word only
                   Examples: "obama face", "kardashian reaction", "rock eyebrow"
                2. If no celebrity/named person/anime characters: primary emotion/action (max 2-3 words)
                   Examples: "cringe reaction", "happy dance", "confused look"
               \s
                Rules:
                - Keep it SHORT: 2-3 words maximum
                - When celebrity is present: "[name] + [generic term]" (face/reaction/moment)
                - When no celebrity: "[emotion] + [action/noun]"
                - Use high-volume search terms people actually type
                - Separate with spaces only
               \s
                Bad examples (too specific):
                - "ishowspeed cringe face" → use "ishowspeed face"
                - "cat knocking over guilty" → use "cat guilty"
               \s
                Output only the keywords, nothing else.\s
       \s""";

}
