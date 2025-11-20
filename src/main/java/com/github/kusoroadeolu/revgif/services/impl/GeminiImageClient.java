package com.github.kusoroadeolu.revgif.services.impl;

import com.github.kusoroadeolu.revgif.configprops.GeminiConfigProperties;
import com.github.kusoroadeolu.revgif.dtos.ImageClientResponse;
import com.github.kusoroadeolu.revgif.dtos.wrappers.HashWrapper;
import com.github.kusoroadeolu.revgif.exceptions.ImageClientException;
import com.github.kusoroadeolu.revgif.mappers.LogMapper;
import com.github.kusoroadeolu.revgif.services.ImageClient;
import com.google.genai.Client;
import com.google.genai.types.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

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

    @Retryable(includes = ImageClientException.class)
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
            throw new ImageClientException(e);
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
            
            Priority order:
            1. If real celebrity/public figure → "[name] [emotion/action]"
               Examples: "obama laugh", "rock eyebrow", "kardashian crying"
            
            2. If anime/cartoon character:
               - If recognizable character → "[character] [emotion]" OR "[show] [emotion]"
                 Examples: "goku power", "naruto run", "demon slayer shock"
               - If generic anime style → "anime [emotion]"
                 Examples: "anime blush", "anime cry", "anime confused"
            
            3. If meme/reaction format → describe the reaction type
               Examples: "awkward silence", "internal screaming", "visible confusion"
            
            4. If clear emotion/action → "[emotion] [optional context]"
               Examples: "excited", "facepalm", "eye roll", "happy dance"
            
            5. If abstract/weird → focus on visual mood or closest emotion
               Examples: "chaotic", "cursed", "wtf", "confused"
            
            Rules:
            - Maximum 3 words, prefer 2
            - Use common search terms people actually type
            - For anime: include "anime" keyword if character not famous
            - Avoid overly specific descriptions
            - Choose high-volume terms over precise descriptions
            
            Output ONLY the search keywords, nothing else.
            """;

}
